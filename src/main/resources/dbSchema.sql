CREATE TABLE TestingOrder (
  id                        INTEGER PRIMARY KEY AUTOINCREMENT,
  partNo                    TEXT NOT NULL, 
  capacity                  REAL NOT NULL,
  voltage                   INTEGER NOT NULL,
  leakCurrent               TEXT NOT NULL,
  dxValue                   REAL NOT NULL,
  marginOfError             TEXT NOT NULL,
  testingTime               INTEGER NOT NULL,
  testingInterval           INTEGER NOT NULL,
  daughterBoard             INTEGER NOT NULL,
  testingBoard              INTEGER NOT NULL,
  tbUUID                    TEXT,
  startTime                 INTEGER,
  lastTestTime              INTEGER,
  currentStatus             INTEGER NOT NULL,
  isRoomTemperatureTested   BOOLEAN NOT NULL
);

CREATE TABLE OvenTestingResult (
  testingID         INTEGER NOT NULL,
  capacityID        INTEGER NOT NULL,
  capacity          REAL    NOT NULL,
  dxValue           REAL    NOT NULL,
  isCapacityOK      BOOLEAN NOT NULL,
  isDXValueOK       BOOLEAN NOT NULL,
  isLeakCurrentOK   BOOLEAN NOT NULL,
  isOK              BOOLEAN NOT NULL,
  timestamp         INTEGER NOT NULL
);

CREATE TABLE RoomTemperatureTestingResult (
  testingID         INTEGER NOT NULL,
  capacityID        INTEGER NOT NULL,
  capacity          REAL    NOT NULL,
  dxValue           REAL    NOT NULL,
  isCapacityOK      BOOLEAN NOT NULL,
  isDXValueOK       BOOLEAN NOT NULL,
  isLeakCurrentOK   BOOLEAN NOT NULL,
  isOK              BOOLEAN NOT NULL,
  timestamp         INTEGER NOT NULL
);

CREATE TABLE OvenTestingQueue (
  testingID       INTEGER NOT NULL,
  insertTime      INTEGER
);

CREATE TABLE OvenUUIDCheckingQueue (
  testingID       INTEGER NOT NULL,
  insertTime      INTEGER NOT NULL,
  currentStatus   INTEGER NOT NULL
);

CREATE TABLE RoomTemperatureTestingQueue (
  testingID       INTEGER NOT NULL,
  insertTime      INTEGER NOT NULL,
  currentStatus   INTEGER NOT NULL
);

CREATE TABLE OvenTestingErrorLog (
  testingID   INTEGER NOT NULL,
  capacityID  INTEGER NOT NULL,
  timestamp   INTEGER NOT NULL,
  message     TEXT NOT NULL
);

CREATE TABLE RoomTemperatureTestingErrorLog (
  testingID   INTEGER NOT NULL,
  capacityID  INTEGER NOT NULL,
  timestamp   INTEGER NOT NULL,
  message     TEXT NOT NULL
);

