# Python-API with flask

This is the API that will be used to communicate between the database and the android application.

## Setup

```cmd
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

You will need to run the activation script to the virtual environment again if you close the command prompt.

```cmd
.venv\Scripts\activate
```

## Running the program

After installing the dependencies you can run the program.

```python
python main.py
```

# Methods

These are the routes and methods that can be used. Note that the timestamp-arguments use a linux timestamp.

## GET
```
/api/rates/daily_by_hour/<timestamp>
```
Returns a sequence of electricity prices for the given day down to the hour.
```
/api/rates/monthly_by_day/<timestamp>
```
Returns a sequence of electricity prices for the given month down to the day.
```
/api/rates/yearly_by_month/<timestamp>
```
Returns a sequence of electricity prices for the given year down to the month.

# Updating the database prices

To get up-to-date electricity prices, insert the data from the EnergyECS API to the database by running the updater program.

```python
python updateprices.py
```