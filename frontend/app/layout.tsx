import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Material Maker",
  description: "素材制作网站 - 模板浏览、上传素材、异步模拟制作、历史记录",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}
