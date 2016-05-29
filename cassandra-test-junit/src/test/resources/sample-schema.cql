CREATE TABLE timeseries (sensor INT,
                         at TIMEUUID,
                         PRIMARY KEY (sensor, at));
