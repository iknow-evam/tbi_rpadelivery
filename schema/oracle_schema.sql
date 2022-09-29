CREATE SEQUENCE INT_RESOURCE_TEMPLATE_SEQ;

CREATE TABLE INT_RESOURCE_TEMPLATE
(
    ID                 NUMBER(22) PRIMARY KEY NOT NULL,
    COMMUNICATION_CODE VARCHAR2(255),
    CONTENT            CLOB,
    SCENARIO_NAME      VARCHAR2(255),
    SCENARIO_VERSION   NUMBER(22),
    TYPE               VARCHAR2(255),
    UNIQUE (COMMUNICATION_CODE, SCENARIO_NAME, SCENARIO_VERSION)
);

CREATE TABLE INT_CUSTOM_COMM_STATUS
(
    ID                 NUMBER(19) NOT NULL PRIMARY KEY,
    COMMUNICATION_UUID VARCHAR2(50 CHAR)
        CONSTRAINT INT_CUSTOM_COMM_STATUS_UUID_ID UNIQUE,
    PROVIDER           VARCHAR2(250 CHAR),
    PROVIDER_RESULT_ID VARCHAR2(250 CHAR),
    REASON             VARCHAR2(250 CHAR),
    STATUS             VARCHAR2(50 CHAR),
    STATUS_UPDATE_TIME TIMESTAMP(6)
)