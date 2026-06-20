-- =====================================
-- 修复 beta_user 表的 RLS 策略 - 允许插入
-- =====================================

-- 1. 先查看现有的策略
SELECT * FROM pg_policies WHERE tablename = 'beta_user';

-- 2. 创建允许任何人插入的策略（用于申请表单）
CREATE POLICY "允许任何人申请内测"
ON beta_user
FOR INSERT
TO anon, authenticated
WITH CHECK (true);

-- 3. 如果上面的策略不行，试试只允许已登录用户插入
-- CREATE POLICY "允许已登录用户申请内测"
-- ON beta_user
-- FOR INSERT
-- TO authenticated
-- WITH CHECK (true);

-- 4. 验证策略是否创建成功
SELECT * FROM pg_policies WHERE tablename = 'beta_user';

-- =====================================
-- 执行完后，刷新网页重新申请
-- =====================================
