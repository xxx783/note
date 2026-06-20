-- =====================================
-- 添加版本下载链接
-- =====================================

-- 1. 查看当前 version 表的所有数据
SELECT id, version, jl, update_time, download_url, is_beta FROM version ORDER BY id DESC;

-- 2. 更新现有版本的下载链接（替换为您的实际下载链接）
UPDATE version 
SET download_url = 'https://your-download-link.com/app-release.apk'
WHERE id = 1;  -- 替换为实际的版本 ID

-- 3. 或者插入一个新版本（带下载链接）
INSERT INTO version (version, jl, update_time, download_url, is_beta) 
VALUES (
    '1.2.0',  -- 版本号
    '修复了一些已知问题，优化了性能',  -- 更新日志
    '2026-04-19 12:00:00',  -- 更新时间
    'https://your-download-link.com/app-release.apk',  -- 下载链接（替换为实际链接）
    false  -- 是否为内测版本
);

-- 4. 验证数据
SELECT id, version, download_url, is_beta FROM version ORDER BY id DESC;

-- =====================================
-- 执行完后，重新运行应用检查更新
-- 展开版本卡片，应该能看到下载按钮了！
-- =====================================
