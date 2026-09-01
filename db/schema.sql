-- Schema MySQL di TutorLink, usato solo dalla full-version con
-- persistence.provider=DBMS. Va eseguito una volta sola:
--     mysql -u root -p < db/schema.sql

CREATE DATABASE IF NOT EXISTS tutorlink
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utenza dedicata all'applicazione. Le credenziali valgono solo per il
-- database locale di prova e sono le stesse scritte in config.properties.
CREATE USER IF NOT EXISTS 'tutorlink'@'localhost' IDENTIFIED BY 'tutorlink';
GRANT ALL PRIVILEGES ON tutorlink.* TO 'tutorlink'@'localhost';
FLUSH PRIVILEGES;

USE tutorlink;

-- Studenti e tutor stanno nella stessa tabella: li distingue la colonna role.
-- hourly_rate e subjects restano vuote per gli studenti.
CREATE TABLE IF NOT EXISTS app_user (
  email          VARCHAR(120) PRIMARY KEY,
  full_name      VARCHAR(120)  NOT NULL,
  password_hash  VARCHAR(128)  NOT NULL,
  role           VARCHAR(10)   NOT NULL,
  hourly_rate    DECIMAL(10,2) NULL,
  subjects       VARCHAR(255)  NULL
);

-- Slot pubblicati dai tutor. reserved vale 1 quando lo slot e' gia' prenotato.
CREATE TABLE IF NOT EXISTS availability (
  id            BIGINT PRIMARY KEY,
  tutor_email   VARCHAR(120) NOT NULL,
  start_at      DATETIME     NOT NULL,
  minutes       INT          NOT NULL,
  reserved      TINYINT(1)   NOT NULL DEFAULT 0,
  CONSTRAINT fk_avail_tutor FOREIGN KEY (tutor_email) REFERENCES app_user(email)
);

-- Le lezioni. availability_id tiene traccia dello slot di partenza, cosi' al
-- riavvio l'applicazione sa quale slot liberare se la lezione viene annullata.
CREATE TABLE IF NOT EXISTS lesson (
  id             BIGINT PRIMARY KEY,
  student_email  VARCHAR(120)  NOT NULL,
  tutor_email    VARCHAR(120)  NOT NULL,
  subject        VARCHAR(40)   NOT NULL,
  start_at       DATETIME      NOT NULL,
  minutes        INT           NOT NULL,
  price          DECIMAL(10,2) NOT NULL,
  state          VARCHAR(20)   NOT NULL,
  meeting_link   VARCHAR(255)  NULL,
  availability_id BIGINT       NULL,
  CONSTRAINT fk_lesson_student FOREIGN KEY (student_email) REFERENCES app_user(email),
  CONSTRAINT fk_lesson_tutor   FOREIGN KEY (tutor_email)   REFERENCES app_user(email)
);

-- Materiale condiviso dal tutor: le informazioni sul file nelle colonne, il
-- contenuto vero e proprio nel BLOB.
CREATE TABLE IF NOT EXISTS material (
  id            BIGINT PRIMARY KEY,
  lesson_id     BIGINT       NOT NULL,
  title         VARCHAR(120) NOT NULL,
  file_name     VARCHAR(160) NOT NULL,
  size_bytes    BIGINT       NOT NULL,
  status        VARCHAR(20)  NOT NULL,
  published_at  DATETIME     NULL,
  content       LONGBLOB     NULL,
  CONSTRAINT fk_material_lesson FOREIGN KEY (lesson_id) REFERENCES lesson(id)
);

-- Un voto per lezione, quindi la chiave e' direttamente l'id della lezione.
CREATE TABLE IF NOT EXISTS feedback (
  lesson_id   BIGINT PRIMARY KEY,
  score       INT          NOT NULL,
  comment     VARCHAR(500) NULL,
  created_at  DATETIME     NOT NULL,
  CONSTRAINT fk_feedback_lesson FOREIGN KEY (lesson_id) REFERENCES lesson(id)
);

-- Notifiche mostrate nella casella dell'utente. seen vale 1 dopo la lettura.
CREATE TABLE IF NOT EXISTS notification (
  id              BIGINT PRIMARY KEY,
  recipient_email VARCHAR(120) NOT NULL,
  message         VARCHAR(500) NOT NULL,
  created_at      DATETIME     NOT NULL,
  seen            TINYINT(1)   NOT NULL DEFAULT 0,
  CONSTRAINT fk_notif_user FOREIGN KEY (recipient_email) REFERENCES app_user(email)
);
