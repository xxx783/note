-- =====================================
-- 修复 beta_user 表的 RLS 策略
-- =====================================

-- 1. 先检查当前的 RLS 状态和策略
SELECT 
    schemaname,
    tablename,
    rowsecurity as rls_enabled
FROM pg_tables 
WHERE tablename = 'beta_user';

-- 查看现有的策略
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual,
    with_check
FROM pg_policies 
WHERE tablename = 'beta_user';

-- 2. 如果 RLS 未启用，先启用（通常不需要）
-- ALTER TABLE beta_user ENABLE ROW LEVEL SECURITY;

-- 3. 删除现有的策略（如果有）
-- DROP POLICY IF EXISTS "允许已登录用户查询内测名单" ON beta_user;
-- DROP POLICY IF EXISTS "允许任何人查询内测名单" ON beta_user;

-- 4. 创建新的策略：允许已登录用户查询
CREATE POLICY "允许已登录用户查询内测名单"
ON beta_user
FOR SELECT
TO authenticated
USING (true);

-- 5. 或者更宽松的策略：允许任何人查询（包括未登录用户）
-- 如果上面的策略不行，试试这个
CREATE POLICY "允许任何人查询内测名单"
ON beta_user
FOR SELECT
TO anon, authenticated
USING (true);

-- 6. 验证策略是否创建成功
SELECT * FROM pg_policies WHERE tablename = 'beta_user';

-- 7. 测试查询（替换为您的邮箱）
SELECT * FROM beta_user WHERE email = 'makabaka204@gmail.com';

-- =====================================
-- 执行完后，重新运行应用检查更新
-- =====================================
