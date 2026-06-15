import os
import sys

# 强行把当前目录塞进 Python 的搜索路径，彻底解决找不到 app 的问题
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.db.session import engine
from app.models.base import Base

def init_database():
    print("正在为【红薯快写】初始化本地 SQLite 数据库表结构...")
    try:
        # 创建所有在 models 里定义的表
        Base.metadata.create_all(bind=engine)
        print("🎉 恭喜！所有数据表（users, generations 等）初始化成功！")
    except Exception as e:
        print(f"❌ 初始化失败，错误原因: {e}")

if __name__ == "__main__":
    init_database()