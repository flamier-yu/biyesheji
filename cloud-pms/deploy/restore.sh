#!/bin/bash
# ============================================================
# 云协同 PMS - 数据库恢复脚本
# ============================================================
# 作用：
#   从 mysqldump 生成的 .sql 或 .sql.gz 文件恢复数据库。
#   支持恢复到 Docker 容器或宿主机上的 MySQL。
#
# 用法：
#   ./deploy/restore.sh backups/biyesheji_20260915_020000.sql.gz
#   DB_MODE=host DB_HOST=192.168.1.10 ./deploy/restore.sh backup.sql.gz
#
# ⚠️ 警告：恢复会覆盖同名数据库中的现有数据！
#    脚本会在执行前要求二次确认。
# ============================================================

set -euo pipefail

# ---------------- 配置 ----------------
DB_MODE="${DB_MODE:-container}"
CONTAINER_NAME="${CONTAINER_NAME:-pms-mysql}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-root}"
DB_NAME="${DB_NAME:-biyesheji}"

BACKUP_FILE="${1:-}"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

die() {
    log "错误：$*"
    exit 1
}

# ---------------- 参数校验 ----------------
[ -n "$BACKUP_FILE" ] || die "用法：$0 <备份文件路径>"
[ -f "$BACKUP_FILE" ] || die "备份文件不存在：$BACKUP_FILE"

case "$BACKUP_FILE" in
    *.gz) IS_GZIP=1 ;;
    *.sql) IS_GZIP=0 ;;
    *) die "只支持 .sql 或 .sql.gz 文件" ;;
esac

log "========== 恢复确认 =========="
log "备份文件：$BACKUP_FILE"
log "目标数据库：$DB_NAME（模式：$DB_MODE）"
log ""
log "⚠️  此操作将覆盖 $DB_NAME 库中的现有数据，且不可撤销！"
read -r -p "确认继续？输入 yes 后回车：" CONFIRM
[ "$CONFIRM" = "yes" ] || { log "已取消"; exit 0; }

# ---------------- 解压到临时文件 ----------------
TMP_SQL=""
cleanup() {
    [ -n "$TMP_SQL" ] && [ -f "$TMP_SQL" ] && rm -f "$TMP_SQL"
}
trap cleanup EXIT

if [ "$IS_GZIP" -eq 1 ]; then
    TMP_SQL=$(mktemp /tmp/pms_restore_XXXXXX.sql)
    log "解压备份文件..."
    gunzip -c "$BACKUP_FILE" > "$TMP_SQL"
else
    TMP_SQL="$BACKUP_FILE"
fi

TABLE_COUNT=$(grep -c "CREATE TABLE" "$TMP_SQL" || true)
log "备份文件包含 $TABLE_COUNT 张表"

# ---------------- 执行恢复 ----------------
COMMON_OPTS="--default-character-set=utf8mb4"

if [ "$DB_MODE" = "container" ]; then
    docker ps --format '{{.Names}}' | grep -qx "$CONTAINER_NAME" \
        || die "容器 $CONTAINER_NAME 未运行"

    log "重建数据库 $DB_NAME ..."
    docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASS" $COMMON_OPTS \
        -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

    log "导入数据（文件较大时可能需要几分钟）..."
    docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASS" $COMMON_OPTS "$DB_NAME" < "$TMP_SQL"
else
    log "重建数据库 $DB_NAME ..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" $COMMON_OPTS \
        -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

    log "导入数据..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" $COMMON_OPTS "$DB_NAME" < "$TMP_SQL"
fi

# ---------------- 校验 ----------------
log "校验恢复结果..."

if [ "$DB_MODE" = "container" ]; then
    ACTUAL=$(docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASS" $COMMON_OPTS -N -B \
        -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME';")
    USER_CNT=$(docker exec -i "$CONTAINER_NAME" mysql -u"$DB_USER" -p"$DB_PASS" $COMMON_OPTS -N -B \
        -e "SELECT COUNT(*) FROM $DB_NAME.sys_user;" 2>/dev/null || echo "读取失败")
else
    ACTUAL=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" $COMMON_OPTS -N -B \
        -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME';")
    USER_CNT=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" $COMMON_OPTS -N -B \
        -e "SELECT COUNT(*) FROM $DB_NAME.sys_user;" 2>/dev/null || echo "读取失败")
fi

log "恢复后表数量：$ACTUAL（备份文件中为 $TABLE_COUNT）"
log "用户记录数：$USER_CNT"

if [ "$ACTUAL" -ne "$TABLE_COUNT" ]; then
    log "⚠️  表数量不一致，请检查导入日志"
    exit 1
fi

log "========== 恢复完成 =========="
log "提示：若后端服务正在运行，建议重启以清理连接池中的旧连接"
