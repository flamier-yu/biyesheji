#!/usr/bin/env node
/**
 * ============================================================
 * PMS 模板改名工具
 * ============================================================
 * 作用：把当前模板工程整体复制一份，并把项目标识批量替换为新项目，
 *      包括 Java 包路径、工程名、数据库名、端口、主题色等。
 *
 * 用法：
 *   1) 复制配置模板并修改：
 *        cp tools/template.config.json tools/my-project.json
 *   2) 预览将要发生的替换（不改任何文件）：
 *        node tools/rename.mjs --config tools/my-project.json --dry-run
 *   3) 正式生成新项目到指定目录：
 *        node tools/rename.mjs --config tools/my-project.json --out D:/projects/xxx
 *
 * 说明：
 *   - 自动跳过 node_modules / target / dist / .git / logs / uploads 等目录
 *   - 二进制文件（图片、字体等）直接复制，不做内容替换
 *   - 会重建 Java 包目录结构，并输出替换统计与变更清单
 */

import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const TEMPLATE_ROOT = path.resolve(__dirname, '..')

/** 模板中原有的标识（改名前） */
const TEMPLATE_IDENTITY = {
  javaPackage: 'com.biyesheji.pms',
  artifactId: 'cloud-pms',
  backendModule: 'pms-server',
  frontendModule: 'pms-web',
  dbName: 'biyesheji',
  projectNameCn: '云协同 PMS',
  projectNameEn: 'CloudPMS',
  serverPort: '8080',
  webPort: '5173',
  themeColor: '#409eff'
}

/** 需要跳过的目录名 */
const SKIP_DIRS = new Set([
  'node_modules', 'target', 'dist', '.git', '.workbuddy', '.idea', '.vscode',
  'logs', 'uploads', '.cache'
])

/** 二进制/不参与文本替换的扩展名 */
const BINARY_EXT = new Set([
  '.png', '.jpg', '.jpeg', '.gif', '.bmp', '.ico', '.webp',
  '.woff', '.woff2', '.ttf', '.eot', '.zip', '.rar', '.7z',
  '.jar', '.war', '.class', '.xlsx', '.xls', '.docx', '.doc', '.pdf'
])

/* ==================== 参数解析 ==================== */

function parseArgs(argv) {
  const args = { dryRun: false }
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i]
    if (a === '--config') args.config = argv[++i]
    else if (a === '--out') args.out = argv[++i]
    else if (a === '--dry-run') args.dryRun = true
    else if (a === '--help' || a === '-h') args.help = true
  }
  return args
}

function printHelp() {
  console.log(`
PMS 模板改名工具

用法:
  node tools/rename.mjs --config <配置文件> [--out <输出目录>] [--dry-run]

参数:
  --config    配置文件路径（参考 tools/template.config.json）
  --out       输出目录；不传则只做预览，不生成文件
  --dry-run   只打印替换计划，不写任何文件
  --help      显示帮助

示例:
  node tools/rename.mjs --config tools/my-project.json --dry-run
  node tools/rename.mjs --config tools/my-project.json --out D:/projects/student-a
`)
}

/* ==================== 替换规则构建 ==================== */

/**
 * 构建替换规则。
 * 关键点：必须按「源字符串长度降序」执行，
 * 否则 com.biyesheji.pms 会先被 biyesheji 的规则破坏掉。
 */
function buildReplacements(cfg) {
  const pairs = [
    // Java 包名（最长，必须最先替换）
    [TEMPLATE_IDENTITY.javaPackage, cfg.javaPackage],
    // 中文项目名（含空格，作为整体替换）
    [TEMPLATE_IDENTITY.projectNameCn, cfg.projectNameCn],
    // 英文项目名
    [TEMPLATE_IDENTITY.projectNameEn, cfg.projectNameEn],
    // 工程/模块名
    [TEMPLATE_IDENTITY.artifactId, cfg.artifactId],
    [TEMPLATE_IDENTITY.backendModule, cfg.backendModule],
    [TEMPLATE_IDENTITY.frontendModule, cfg.frontendModule],
    // 数据库名（放在包名之后，避免破坏包路径）
    [TEMPLATE_IDENTITY.dbName, cfg.dbName]
  ]

  const extra = cfg.extraReplacements || {}
  Object.entries(extra).forEach(([from, to]) => pairs.push([from, to]))

  return pairs
    .filter(([from, to]) => from && to && from !== to)
    .sort((a, b) => b[0].length - a[0].length)
}

function applyReplacements(text, rules, stats) {
  let result = text
  for (const [from, to] of rules) {
    if (!result.includes(from)) continue
    const count = result.split(from).length - 1
    result = result.split(from).join(to)
    stats.totalHits += count
    stats.byRule[from] = (stats.byRule[from] || 0) + count
  }
  return result
}

/* ==================== 文件遍历与处理 ==================== */

function walk(dir, onFile) {
  const entries = fs.readdirSync(dir, { withFileTypes: true })
  for (const entry of entries) {
    if (entry.name.startsWith('.') && entry.name !== '.env.example') {
      // 跳过隐藏目录/文件，但保留 .env.example 这类模板
      if (entry.isDirectory()) continue
    }
    if (SKIP_DIRS.has(entry.name)) continue

    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      walk(full, onFile)
    } else {
      onFile(full)
    }
  }
}

/**
 * 计算目标相对路径：包目录结构也要跟着改名
 */
function mapTargetPath(relPath, cfg) {
  const oldPkgPath = TEMPLATE_IDENTITY.javaPackage.replace(/\./g, '/')
  const newPkgPath = cfg.javaPackage.replace(/\./g, '/')

  let result = relPath.split(path.sep).join('/')

  // 1) 先把包路径整体替换（com/biyesheji/pms -> com/newco/newapp）
  result = result.split(oldPkgPath).join(newPkgPath)

  // 2) 再逐段做其他标识替换
  const segRules = [
    [TEMPLATE_IDENTITY.backendModule, cfg.backendModule],
    [TEMPLATE_IDENTITY.frontendModule, cfg.frontendModule],
    [TEMPLATE_IDENTITY.artifactId, cfg.artifactId]
  ]
  for (const [from, to] of segRules) {
    result = result.split(from).join(to)
  }

  return result.split('/').join(path.sep)
}

/* ==================== 主流程 ==================== */

function main() {
  const args = parseArgs(process.argv.slice(2))
  if (args.help) {
    printHelp()
    return
  }
  if (!args.config) {
    console.error('❌ 缺少 --config 参数，用 --help 查看用法')
    process.exit(1)
  }

  const configPath = path.resolve(process.cwd(), args.config)
  if (!fs.existsSync(configPath)) {
    console.error(`❌ 配置文件不存在：${configPath}`)
    process.exit(1)
  }
  const cfg = JSON.parse(fs.readFileSync(configPath, 'utf8'))

  // 校验必填项
  const required = ['javaPackage', 'artifactId', 'backendModule', 'frontendModule', 'dbName', 'projectNameCn']
  const missing = required.filter((k) => !cfg[k])
  if (missing.length) {
    console.error(`❌ 配置缺少必填项：${missing.join(', ')}`)
    process.exit(1)
  }
  if (!/^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$/.test(cfg.javaPackage)) {
    console.error(`❌ javaPackage 格式不合法（需形如 com.company.app）：${cfg.javaPackage}`)
    process.exit(1)
  }

  const rules = buildReplacements(cfg)
  const stats = { files: 0, changedFiles: 0, totalHits: 0, byRule: {}, binaryCopied: 0 }
  const changedList = []

  const outRoot = args.out ? path.resolve(process.cwd(), args.out) : null
  const targetBaseName = cfg.artifactId

  console.log('============================================')
  console.log('  PMS 模板改名工具')
  console.log('============================================')
  console.log(`模板目录 : ${TEMPLATE_ROOT}`)
  console.log(`配置来源 : ${configPath}`)
  console.log(`输出目录 : ${outRoot ? path.join(outRoot, targetBaseName) : '（仅预览，不生成文件）'}`)
  console.log('')
  console.log('替换规则（按长度降序执行）：')
  rules.forEach(([from, to]) => console.log(`  ${from}  ->  ${to}`))
  console.log('')

  if (outRoot && !args.dryRun) {
    fs.mkdirSync(outRoot, { recursive: true })
  }

  walk(TEMPLATE_ROOT, (srcFile) => {
    stats.files++
    const relPath = path.relative(TEMPLATE_ROOT, srcFile)
    const ext = path.extname(srcFile).toLowerCase()
    const targetRel = mapTargetPath(relPath, cfg)

    if (args.dryRun || !outRoot) {
      if (!BINARY_EXT.has(ext)) {
        const content = fs.readFileSync(srcFile, 'utf8')
        const preview = { totalHits: 0, byRule: {} }
        const replaced = applyReplacements(content, rules, preview)
        if (preview.totalHits > 0) {
          stats.changedFiles++
          stats.totalHits += preview.totalHits
          Object.entries(preview.byRule).forEach(([k, v]) => {
            stats.byRule[k] = (stats.byRule[k] || 0) + v
          })
          changedList.push(`${relPath}  (${preview.totalHits} 处)`)
        }
      }
      return
    }

    // 正式生成
    const destFile = path.join(outRoot, targetBaseName, targetRel)
    fs.mkdirSync(path.dirname(destFile), { recursive: true })

    if (BINARY_EXT.has(ext)) {
      fs.copyFileSync(srcFile, destFile)
      stats.binaryCopied++
      return
    }

    const content = fs.readFileSync(srcFile, 'utf8')
    const preview = { totalHits: 0, byRule: {} }
    const replaced = applyReplacements(content, rules, preview)
    fs.writeFileSync(destFile, replaced, 'utf8')

    if (preview.totalHits > 0) {
      stats.changedFiles++
      stats.totalHits += preview.totalHits
      Object.entries(preview.byRule).forEach(([k, v]) => {
        stats.byRule[k] = (stats.byRule[k] || 0) + v
      })
      changedList.push(`${targetRel}  (${preview.totalHits} 处)`)
    }
  })

  console.log('--------------------------------------------')
  console.log('  统计结果')
  console.log('--------------------------------------------')
  console.log(`扫描文件      : ${stats.files}`)
  console.log(`内容被改文件  : ${stats.changedFiles}`)
  console.log(`替换总次数    : ${stats.totalHits}`)
  console.log(`二进制直接复制: ${stats.binaryCopied}`)
  console.log('')
  console.log('各规则命中次数：')
  Object.entries(stats.byRule)
    .sort((a, b) => b[1] - a[1])
    .forEach(([k, v]) => console.log(`  ${k}  ->  ${v}`))

  if (changedList.length) {
    console.log('')
    console.log('涉及文件（前 30 条）：')
    changedList.slice(0, 30).forEach((l) => console.log('  ' + l))
    if (changedList.length > 30) {
      console.log(`  ... 其余 ${changedList.length - 30} 个文件省略`)
    }
  }

  console.log('')
  if (args.dryRun || !outRoot) {
    console.log('ℹ️  当前为预览模式，未生成任何文件。加上 --out 参数即可正式生成。')
  } else {
    console.log(`✅ 生成完成：${path.join(outRoot, targetBaseName)}`)
    console.log('')
    console.log('接下来还需要手工处理的事项：')
    console.log('  1. 建库并导入 SQL：sql/01-schema.sql -> 02-data.sql -> 03-business-menu.sql')
    console.log(`     create database ${cfg.dbName} default charset utf8mb4;`)
    console.log('  2. 检查 application-dev.yml 的数据源账号密码')
    console.log('  3. 前端：cd ' + cfg.frontendModule + ' && npm install')
    console.log('  4. 后端：cd ' + cfg.backendModule + ' && mvn clean package -DskipTests')
    console.log(`  5. 启动后端必须显式指定端口：--server.port=${cfg.serverPort || 8080}`)
  }
}

main()
