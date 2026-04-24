import sqlite3
import os

db_path = r"c:\Users\Rodolfo\Desktop\ficha gurps\ficha-gurps\gurps_app\gurps-ficha-android\scratch\gurps_db_debug.db"

if not os.path.exists(db_path):
    print(f"Erro: Arquivo {db_path} não encontrado.")
    exit(1)

try:
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    print("--- PESQUISA POR 'DESEJO' ---")
    cursor.execute("SELECT title, summary, category FROM graph_nodes WHERE title LIKE '%Desejo%'")
    rows = cursor.fetchall()
    for row in rows:
        print(f"Título: {row[0]}")
        print(f"Categoria: {row[2]}")
        print(f"Resumo: {row[1][:100]}...")
        print("-" * 20)

    print("\n--- PESQUISA POR 'MAGIA' ---")
    cursor.execute("SELECT title, summary, category FROM graph_nodes WHERE title LIKE '%Magia%' LIMIT 5")
    rows = cursor.fetchall()
    for row in rows:
        print(f"Título: {row[0]}")
        print("-" * 20)

    conn.close()
except Exception as e:
    print(f"Erro ao ler banco: {e}")
