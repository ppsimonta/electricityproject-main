import mysql
import mysql.connector
import requests
import pytz
from datetime import datetime
from datetime import datetime, timedelta

connection = None
cursor = None

try:

    connection = mysql.connector.connect(database='pricechecker_database', user='root')
    cursor = connection.cursor(prepared=True)

    utc_timezone = pytz.timezone('UTC')
    query = "INSERT IGNORE INTO time_and_price (time, value) VALUES (%s, %s)"
    
    
    tomorrowdate = datetime.today() + timedelta(days=1)
    tomorrowdate_str = tomorrowdate.strftime('%Y-%m-%d')+'T00:00:00Z'
    print(tomorrowdate_str) 
    req = requests.get(f'http://spotprices.energyecs.frostbit.fi/api/v1/prices/byday/{tomorrowdate_str}')
    prices = req.json()

    for price in prices:

        utc_time_str = price['_time']
        utc_time = datetime.strptime(utc_time_str, '%Y-%m-%dT%H:%M:%SZ')
    
        utc_plus_3_timezone = pytz.timezone('Etc/GMT+3')
        utc_plus_3_time = utc_timezone.localize(utc_time).astimezone(utc_plus_3_timezone)

        time = utc_plus_3_time.strftime('%Y-%m-%dT%H:%M:%S')
        value = price['value']

        values = (time, value)
        cursor.execute(query, values) 
    connection.commit()
    print('Tomorrow´s prices fetched')


except Exception as e:
    print(e)
    connection.rollback()

finally:
    if cursor is not None:
        cursor.close()
    if connection is not None and connection.is_connected():
        connection.close()