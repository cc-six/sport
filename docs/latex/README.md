# 电子科技大学中山学院毕业论文 LaTeX 模板

基于 `zscthesis` 文档类的毕业设计（论文）LaTeX 模板，适用于电子科技大学中山学院本科毕业论文撰写。

## 目录结构

```
latex-template/
├── main.tex              # 主入口文件，编译此文件生成PDF
├── style/                # 样式文件（一般无需修改）
│   ├── zscthesis.cls     # 文档类定义
│   ├── zscthesis.cfg     # 配置文件
│   ├── zsccode.sty       # 代码高亮样式
│   └── zscexample.sty    # 示例样式
├── bib/                  # 参考文献
│   ├── ref.bib           # 参考文献数据库（在此添加文献条目）
│   └── gbt7714-numerical.bst  # 国标参考文献样式
├── logo/                 # 校徽等Logo资源
│   ├── logo.png
│   └── uestc.pdf
├── img/                  # 图片目录（将论文图片放在此文件夹）
└── tex/                  # 论文各章节内容
    ├── frontinfo.tex     # 封面信息（填写个人信息）
    ├── abstract-ch.tex   # 中文摘要
    ├── abstract-en.tex   # 英文摘要
    ├── content.tex       # 目录（自动生成）
    ├── chap-1.tex        # 第1章 引言
    ├── chap-2.tex        # 第2章 相关技术
    ├── chap-3.tex        # 第3章 需求分析
    ├── chap-4.tex        # 第4章 系统设计
    ├── chap-5.tex        # 第5章 系统实现
    ├── chap-6.tex        # 第6章 系统测试
    ├── chap-7.tex        # 第7章 总结与展望
    ├── reference.tex     # 参考文献（自动生成）
    ├── acknowledgement.tex  # 致谢
    └── appendix.tex      # 附录（无内容请在main.tex中注释掉）
```

## 快速开始

### 环境要求

- TeX 发行版：[TeX Live](https://www.tug.org/texlive/)（推荐 2020 及以上版本）或 [MiKTeX](https://miktex.org/)
- 编译引擎：**XeLaTeX**（必须，因为使用了 `fontspec` 和 `xeCJK`）
- 操作系统：Windows / macOS / Linux

### 编译步骤

1. 修改 `tex/frontinfo.tex` 中的个人信息（题目、姓名、学号等）
2. 将论文图片放入 `img/` 目录
3. 在 `bib/ref.bib` 中添加参考文献条目
4. 在 `tex/chap-*.tex` 中撰写各章节内容
5. 编译生成 PDF：

```bash
xelatex main.tex
bibtex main
xelatex main.tex
xelatex main.tex
```

或使用 `latexmk` 一键编译：

```bash
latexmk -xelatex main.tex
```

### VS Code 推荐

安装 [LaTeX Workshop](https://marketplace.visualstudio.com/items?itemName=James-Yu.latex-workshop) 扩展，打开 `main.tex` 即可实时预览和编译。

## 使用说明

### 封面信息

编辑 `tex/frontinfo.tex`，修改以下字段：

| 命令 | 说明 |
|------|------|
| `\mytitle{}` | 中文论文题目 |
| `\MYTITLE{}` | 英文论文题目 |
| `\institute{}` | 教学指导单位 |
| `\major{}` | 专业名称 |
| `\studentid{}` | 学号 |
| `\student{}` | 学生姓名 |
| `\advisor{}` | 指导教师(职称) |
| `\completedate{}` | 完成时间 |

### 插入图片

将图片文件放入 `img/` 目录，在正文中使用：

```latex
\begin{figure}[htbp]
\centering
\includegraphics[width=0.85\textwidth]{img/your-image.png}
\caption{图片标题}
\label{fig:your-label}
\end{figure}
```

### 插入表格

```latex
\begin{table}[htbp]
\centering
\caption{表格标题}
\label{tab:your-label}
\begin{tabular}{|c|c|c|}
\hline
列1 & 列2 & 列3 \\ \hline
数据1 & 数据2 & 数据3 \\ \hline
\end{tabular}
\end{table}
```

### 引用参考文献

1. 在 `bib/ref.bib` 中添加文献条目
2. 在正文中使用 `\cite{key}` 引用，例如 `\cite{author2024journal}`

### 代码高亮

模板内置多种语言代码环境：

```latex
\begin{java}
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
\end{java}
```

支持的语言环境：`java`、`python`、`javascript`、`cpp`、`clan`（C）、`sql`、`html`、`xml`、`php`、`gogo`（Go）、`matlab`、`tex`

## 注意事项

- 必须使用 **XeLaTeX** 编译，不支持 pdfLaTeX
- Windows 下需安装宋体（SimSun）、黑体（SimHei）、楷体（KaiTi）、仿宋（FangSong）字体
- macOS 下使用 Songti SC、Heiti SC、Kaiti SC、STFangsong 字体
- 参考文献格式遵循 GB/T 7714-2015 标准
- 如无附录内容，请在 `main.tex` 中注释掉 `\input{tex/appendix.tex}`

## 致谢

本模板基于 [zsc-cs-latex-thesis](https://gitee.com/yeyunxiaopan/zsc-cs-latex-thesis) 项目，由 Safin (zhaoqid@zsc.edu.cn) 开发维护。
