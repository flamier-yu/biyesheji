# 模板复用工具

把当前项目整体复制一份并替换成新项目，用于「接单 → 改名 → 交付」的批量复用。

## 快速使用

```bash
# 1. 复制配置模板
cp tools/template.config.json tools/my-student.json

# 2. 编辑配置（改成客户的项目信息）
#    至少改：projectNameCn / projectNameEn / artifactId /
#            backendModule / frontendModule / javaPackage / dbName

# 3. 预览（不写任何文件，先确认替换范围）
node tools/rename.mjs --config tools/my-student.json --dry-run

# 4. 正式生成到指定目录
node tools/rename.mjs --config tools/my-student.json --out D:/projects/student-a
```

生成结果：`D:/projects/student-a/<artifactId>/`

## 配置项说明

| 字段 | 说明 | 示例 |
|---|---|---|
| `projectNameCn` | 中文项目名（登录页、页面标题用） | `科研云 SRMS` |
| `projectNameEn` | 英文项目名 | `SRMS` |
| `artifactId` | 项目根目录名 / 外层标识 | `srms` |
| `backendModule` | 后端模块目录名 | `srms-server` |
| `frontendModule` | 前端模块目录名 | `srms-web` |
| `javaPackage` | Java 包名（会自动重建目录结构） | `com.zhangsan.srms` |
| `dbName` | 数据库名 | `srms_db` |
| `serverPort` | 后端端口 | `8081` |
| `webPort` | 前端端口 | `5174` |
| `themeColor` | 主题色（见下方「换主题色」） | `#2e7d32` |
| `extraReplacements` | 自定义额外替换（如公司名、项目编号前缀） | `{"云协同科技有限公司": "示例大学"}` |

### `extraReplacements` 的典型用法

- 换掉演示数据里的公司名：`{"云协同科技有限公司": "某某大学"}`
- 换掉项目编号前缀：`{"PJ2026": "KY2026"}`
- 换掉作者信息：`{"云协同研发中心": "张三"}`

## 脚本做了什么

1. **递归遍历**项目文件，自动跳过 `node_modules` / `target` / `dist` / `.git` / `logs` / `uploads`
2. **按源字符串长度降序执行替换**
   > 这一点很关键：`com.biyesheji.pms` 里包含 `biyesheji`，
   > 如果先替换 `biyesheji` 就会破坏包名，所以包名规则必须排在最前。
3. **重建 Java 包目录结构**：`com/biyesheji/pms` → `com/zhangsan/srms`
4. **二进制文件直接复制**（图片、字体、Office 文档等不做文本替换）
5. **输出统计**：扫描文件数、改动文件数、替换总次数、每条规则命中次数、涉及文件清单

## 换主题色

Element Plus 的主色通过 CSS 变量覆盖，改 `src/styles/index.scss` 顶部即可：

```scss
:root {
  --el-color-primary: #2e7d32;
  --el-color-primary-light-3: #569d59;
  --el-color-primary-light-5: #81b983;
  --el-color-primary-light-7: #abd4ac;
  --el-color-primary-light-8: #c0e0c1;
  --el-color-primary-light-9: #d5ebd6;
  --el-color-primary-dark-2: #256428;
  /* 侧边栏底色，想换成深蓝/深灰也在这里改 */
  --sidebar-bg: #304156;
}
```

`light-3` ~ `light-9` 是主色与白色按比例混合的衍生色，可用在线工具算，或直接用 `color-mix`：

```scss
--el-color-primary-light-3: color-mix(in srgb, var(--el-color-primary) 70%, white);
```

另外 `src/layout/index.vue` 里侧边栏的 `background-color="#304156"` 也要同步改。

## 生成之后还要做什么

脚本执行完会在末尾打印清单，核心是这几步：

```bash
# 1. 建库 + 导入脚本（按顺序）
mysql -uroot -p -e "create database srms_db default charset utf8mb4;"
mysql -uroot -p srms_db < sql/01-schema.sql
mysql -uroot -p srms_db < sql/02-data.sql
mysql -uroot -p srms_db < sql/03-business-menu.sql

# 2. 确认 application-dev.yml 里的数据源账号密码
# 3. 后端编译
cd srms-server && mvn clean package -DskipTests

# 4. 启动（注意：必须显式指定端口）
java -jar target/srms-server.jar --server.port=8081

# 5. 前端
cd srms-web && npm install && npm run dev
```

默认账号：`admin / admin123`

## 注意事项

- 生成的新项目里**也会带一份 `tools/` 目录**，方便继续二次改名，不影响运行
- 改名后**必须重新建库**，不要复用旧库（表结构虽然一样，但菜单权限里的标识可能对不上）
- 如果客户的题目需要增删业务模块，建议在模板上直接改完再复制，而不是复制完再逐个改
