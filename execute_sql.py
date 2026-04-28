import mysql.connector
import sys

def execute_sql(filename):
    try:
        conn = mysql.connector.connect(
            host="localhost",
            user="root",
            password="",
            database="dhc"
        )
        cursor = conn.cursor()
        
        with open(filename, 'r', encoding='utf-8') as f:
            sql_script = f.read()
            
        # Split script by semicolon
        statements = sql_script.split(';')
        for statement in statements:
            if statement.strip():
                cursor.execute(statement)
        
        conn.commit()
        print("SQL script executed successfully!")
        
    except mysql.connector.Error as err:
        print(f"Error: {err}")
    except Exception as e:
        print(f"An error occurred: {e}")
    finally:
        if 'conn' in locals() and conn.is_connected():
            cursor.close()
            conn.close()

if __name__ == "__main__":
    sql_file = "c:/Users/chaima dahdouh/Downloads/DHC_JAVA (4)/DHC_JAVA (2)/DHC_JAVA/Dhc_java/src/main/resources/db/add_annonce_reactions.sql"
    execute_sql(sql_file)
