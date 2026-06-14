CREATE USER care_owner NO AUTHENTICATION
  DEFAULT TABLESPACE users
  QUOTA UNLIMITED ON users
/
CREATE TABLE care_owner.patient_cases (
    id NUMBER PRIMARY KEY,
    patient_name VARCHAR2(80),
    care_team VARCHAR2(40),
    case_status VARCHAR2(30),
    assigned_coordinator VARCHAR2(30),
    needs_clinician_review CHAR(1),
    care_plan_summary VARCHAR2(400),
    coordinator_notes VARCHAR2(400),
    diagnosis VARCHAR2(200),
    sensitive_lab_summary VARCHAR2(400),
    clinician_notes VARCHAR2(400),
    clinician_decision VARCHAR2(80)
)
/
CREATE TABLE care_owner.care_policies (
    id NUMBER PRIMARY KEY,
    title VARCHAR2(120),
    body VARCHAR2(1000),
    audience VARCHAR2(40),
    embedding VECTOR(3, FLOAT32)
)
/
INSERT INTO care_owner.patient_cases VALUES
(1, 'Avery Patel', 'north', 'open', 'CLARA', 'Y',
 'Coordinate discharge planning and follow-up visits.',
 'Needs transport assistance.',
 'Cardiac observation',
 'Troponin trend requires clinician review.',
 'Review medication interaction before discharge.',
 NULL)
/
INSERT INTO care_owner.patient_cases VALUES
(2, 'Morgan Lee', 'north', 'open', 'CLARA', 'N',
 'Arrange home-care check-in.',
 'Family requested afternoon calls.',
 'Mobility limitation',
 'Restricted rehab assessment.',
 NULL,
 NULL)
/
INSERT INTO care_owner.patient_cases VALUES
(3, 'Jordan Kim', 'south', 'open', 'ROBIN', 'Y',
 'Clinician review requested by care team.',
 'Escalated by coordinator.',
 'Respiratory infection',
 'Oxygen saturation trend requires review.',
 'Assess treatment escalation.',
 NULL)
/
INSERT INTO care_owner.care_policies VALUES
(10, 'Coordinator discharge checklist',
 'Care coordinators confirm transport, family contact, and follow-up scheduling.',
 'CARE_COORDINATOR',
 TO_VECTOR('[1,0,0]'))
/
INSERT INTO care_owner.care_policies VALUES
(20, 'Clinician review protocol',
 'Clinicians review diagnosis, lab summary, medication interaction, and treatment decision.',
 'CLINICIAN',
 TO_VECTOR('[0,1,0]'))
/
INSERT INTO care_owner.care_policies VALUES
(30, 'Shared escalation policy',
 'Both care coordinators and clinicians can read escalation routing guidance.',
 'ALL',
 TO_VECTOR('[0,0,1]'))
/
CREATE END USER clara IDENTIFIED BY Care_End_User_2026
/
CREATE END USER drew IDENTIFIED BY Care_End_User_2026
/
CREATE USER care_app IDENTIFIED BY Care_App_2026
/
CREATE OR REPLACE DATA ROLE care_coordinator
/
CREATE OR REPLACE DATA ROLE clinician
/
CREATE ROLE care_coordinator_db_role
/
CREATE ROLE care_clinician_db_role
/
GRANT CREATE SESSION TO care_coordinator_db_role
/
GRANT CREATE SESSION TO care_clinician_db_role
/
GRANT CREATE SESSION TO care_app
/
GRANT UPDATE (clinician_decision) ON care_owner.patient_cases TO care_clinician_db_role
/
GRANT care_coordinator_db_role TO care_coordinator
/
GRANT care_clinician_db_role TO clinician
/
GRANT DATA ROLE care_coordinator TO clara
/
GRANT DATA ROLE clinician TO drew
/
CREATE OR REPLACE DATA GRANT care_owner.cases_for_coordinators
  AS SELECT (ALL COLUMNS EXCEPT diagnosis, sensitive_lab_summary, clinician_notes, clinician_decision)
  ON care_owner.patient_cases
  WHERE assigned_coordinator = ORA_END_USER_CONTEXT.username
  TO care_coordinator
/
CREATE OR REPLACE DATA GRANT care_owner.cases_for_clinician_reads
  AS SELECT (ALL COLUMNS EXCEPT coordinator_notes), UPDATE (clinician_decision)
  ON care_owner.patient_cases
  WHERE needs_clinician_review = 'Y'
  TO clinician
/
CREATE OR REPLACE DATA GRANT care_owner.cases_for_clinician_writes
  AS UPDATE (clinician_decision)
  ON care_owner.patient_cases
  WHERE needs_clinician_review = 'Y'
  TO clinician
/
CREATE OR REPLACE DATA GRANT care_owner.drew_case_write
  AS UPDATE (clinician_decision)
  ON care_owner.patient_cases
  WHERE needs_clinician_review = 'Y'
  TO drew
/
CREATE OR REPLACE DATA GRANT care_owner.policies_for_coordinators
  AS SELECT
  ON care_owner.care_policies
  WHERE audience IN ('CARE_COORDINATOR', 'ALL')
  TO care_coordinator
/
CREATE OR REPLACE DATA GRANT care_owner.policies_for_clinicians
  AS SELECT
  ON care_owner.care_policies
  WHERE audience IN ('CLINICIAN', 'ALL')
  TO clinician
/
SET USE DATA GRANTS ONLY ON care_owner.patient_cases ENABLED
/
SET USE DATA GRANTS ONLY ON care_owner.care_policies ENABLED
/
