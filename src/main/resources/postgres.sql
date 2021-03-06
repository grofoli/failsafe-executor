CREATE TABLE PERSISTENT_TASK (
    ID VARCHAR(36) NOT NULL,
    PARAMETER VARCHAR(200),
    NAME VARCHAR(200) NOT NULL,
    LOCK_TIME TIMESTAMP,
    FAILED SMALLINT DEFAULT 0,
    FAIL_TIME TIMESTAMP,
    EXCEPTION_MESSAGE VARCHAR(1000),
    STACK_TRACE TEXT,
    VERSION INT DEFAULT 0,
    CREATED_DATE TIMESTAMP,
    PRIMARY KEY (ID,NAME)
);