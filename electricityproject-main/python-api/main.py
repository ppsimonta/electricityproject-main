from flask import Flask, jsonify
import mysql.connector
import datetime

def unix_to_sql_day(unix_timestamp):
    # Convert the Unix timestamp to a Python datetime object.
    date_time = datetime.datetime.fromtimestamp(int(unix_timestamp))

    # Format the datetime object as a SQL date string.
    sql_date = date_time.strftime('%d')

    # Return the SQL date string.
    return sql_date

def unix_to_sql_month(unix_timestamp):
    # Convert the Unix timestamp to a Python datetime object.
    date_time = datetime.datetime.fromtimestamp(int(unix_timestamp))

    # Format the datetime object as a SQL date string.
    sql_date = date_time.strftime('%m')

    # Return the SQL date string.
    return sql_date

def unix_to_sql_year(unix_timestamp):
    # Convert the Unix timestamp to a Python datetime object.
    date_time = datetime.datetime.fromtimestamp(int(unix_timestamp))

    # Format the datetime object as a SQL date string.
    sql_date = date_time.strftime('%Y')

    # Return the SQL date string.
    return sql_date


# Credentials
user = 'root'
password = ''


# Database connection
connection = mysql.connector.connect(user=user, password=password, database='pricechecker_database')
cursor = connection.cursor(prepared=True, dictionary=True)

def refresh_connection():
    # check if youre connected, if not, connect again
    global connection
    global cursor
    if (connection.is_connected() == False):
        connection = mysql.connector.connect(user=user, password=password, database='pricechecker_database')
        cursor = connection.cursor(prepared=True, dictionary=True)

app = Flask(__name__)

@app.route('/')
def home():
    return """Server works"""

@app.route('/api/rates/daily_by_hour/<timestamp>', methods=['GET'])
def daily_by_hour(timestamp):
    refresh_connection()
    year = unix_to_sql_year(timestamp)
    month = unix_to_sql_month(timestamp)
    day = unix_to_sql_day(timestamp)
    query = "SELECT DATE_FORMAT(time, '%Y-%m-%dT%H:%i:%S+03:00') AS time, value FROM time_and_price WHERE YEAR(time) = %s AND MONTH(time) = %s AND DAY(time) = %s;"
    cursor.execute(query, (year, month, day))
    prices = cursor.fetchall()
    return jsonify(prices)

@app.route('/api/rates/monthly_by_day/<timestamp>', methods=['GET'])
def monthly_by_day(timestamp):
    refresh_connection()
    month = unix_to_sql_month(timestamp)
    year = unix_to_sql_year(timestamp)
    query = "SELECT DATE_FORMAT(time, '%Y-%m-%dT%H:%i:%S+03:00') AS time, AVG(value) as value FROM time_and_price WHERE MONTH(time) = %s AND YEAR(time) = %s GROUP BY DAY(time), MONTH(time);"
    cursor.execute(query, (month, year))
    prices = cursor.fetchall()
    return jsonify(prices)

@app.route('/api/rates/yearly_by_month/<timestamp>', methods=['GET'])
def yearly_by_month(timestamp):
    refresh_connection()
    year = unix_to_sql_year(timestamp)
    query = "SELECT DATE_FORMAT(time, '%Y-%m-%dT%H:%i:%S+03:00') AS time, AVG(value) as value FROM time_and_price WHERE YEAR(time) = %s GROUP BY YEAR(time), MONTH(time);"
    cursor.execute(query, (year,))
    prices = cursor.fetchall()
    return jsonify(prices)

if __name__ == "__main__":
    app.run(debug=True)