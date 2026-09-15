#!/bin/bash
# ============================================================
# 云协同 PMS - 数据库备份脚本
# ============================================================
# 作用：
#   通过 mysqldump 导出逻辑备份（SQL 文本），压缩后归档，
#   并自动清理超期备份。适用于 Docker 容器化部署，
#   也支持备份宿主机上的 MySQL。
#
# 用法：
#   chmod +x deploy/backup.sh
#   ./deploy/backup.sh                    # 手动执行一次
#   crontab -e                            # 加入定时任务
#   0 2 * * * /opt/cloud-pms/deploy/backup.sh >> /var/log/pms-backup.log 2>&1
#
# 环境变量（可覆盖默认值）：
#   DB_MODE=container|host   数据库运行方式，默认 container
#   DB_HOST / DB_PORT        宿主机模式下的地址与端口
#   DB_USER / DB_PASS        账号密码
#   DB_NAME                  数据库名
#   BACKUP_DIR               备份存放目录
#   KEEP_DAYS                保留天数
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
BACKUP_DIR="${BACKUP_DIR:-./backups}"
KEEP_DAYS="${KEEP_DAYS:-30}"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${TIMESTAMP}.sql"
ARCHIVE_FILE="${BACKUP_FILE}.gz"

# ---------------- 工具函数 ----------------
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

die() {
    log "错误：$*"
    exit 1
}

# ---------------- 前置检查 ----------------
if [ "$DB_MODE" = "container" ]; then
    command -v docker >/dev/null 2>&1 || die "未找到 docker 命令"
    docker ps --format '{{.Names}}' | grep -qx "$CONTAINER_NAME" \
        || die "容器 $CONTAINER_NAME 未运行，请先执行 docker compose up -d"
else
    command -v mysqldump >/dev/null 2>&1 || die "未找到 mysqldump 命令"
fi

mkdir -p "$BACKUP_DIR"

log "========== 开始备份 =========="
log "模式：$DB_MODE　数据库：$DB_NAME　目标：$ARCHIVE_FILE"

# ---------------- 执行备份 ----------------
# 关键参数说明：
#   --single-transaction  开启一致性快照，备份期间不锁表（InnoDB）
#   --routines            导出存储过程与函数
#   --triggers            导出触发器
#   --events              导出事件调度器
#   --set-gtid-purged=OFF 避免导出文件中带有 GTID 信息，便于导入到未开启 GTID 的实例
#   --default-character-set=utf8mb4  防止中文乱码
DUMP_OPTS="--single-transaction --routines --triggers --events \
--set-gtid-purged=OFF --default-character-set=utf8mb4 \
--hex-blob --skip-lock-tables"

if [ "$DB_MODE" = "container" ]; then
    if ! docker exec "$CONTAINER_NAME" \
        mysqldump -u"$DB_USER" -p"$DB_PASS" $DUMP_OPTS "$DB_NAME" > "$BACKUP_FILE"; then
        rm -f "$BACKUP_FILE"
        die "mysqldump 执行失败"
    fi
else
    if ! mysqldump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
        $DUMP_OPTS "$DB_NAME" > "$BACKUP_FILE"; then
        rm -f "$BACKUP_FILE"
        die "mysqldump 执行失败"
    fi
fi

# ---------------- 校验 ----------------
# 备份文件不能为空，且必须包含建表语句，否则视为失败
if [ ! -s "$BACKUP_FILE" ]; then
    rm -f "$BACKUP_FILE"
    die "备份文件为空，可能账号密码错误或数据库不存在"
fi

if ! grep -q "CREATE TABLE" "$BACKUP_FILE"; then
    rm -f "$BACKUP_FILE"
    die "备份文件未包含建表语句，内容异常"
fi

TABLE_COUNT=$(grep -c "CREATE TABLE" "$BACKUP_FILE" || true)
RAW_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)

log "备份校验通过：包含 $TABLE_COUNT 张表，原始大小 $RAW_SIZE"

# ---------------- 压缩 ----------------
gzip -9 "$BACKUP_FILE"
ARCHIVE_SIZE=$(du -h "$ARCHIVE_FILE" | cut -f1)
log "压缩完成：$ARCHIVE_SIZE"

# ---------------- 清理超期备份 ----------------
DELETED=$(find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -type f -mtime +"$KEEP_DAYS" -print -delete | wc -l)
if [ "$DELETED" -gt 0 ]; then
    log "已清理 $DELETED 个超过 $KEEP_DAYS 天的备份"
fi

REMAIN=$(find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -type f | wc -l)
log "当前保留备份数：$REMAIN"

log "========== 备份完成 =========="
