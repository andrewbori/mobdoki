-- Table: "User"

-- DROP TABLE "User";

CREATE TABLE "User"
(
  username text NOT NULL,
  "password" integer NOT NULL,
  usertype text NOT NULL,
  address text,
  "name" text,
  email text,
  CONSTRAINT "Users_pkey" PRIMARY KEY (username),
  CONSTRAINT "usertypeCheck" CHECK (usertype = 'doctor'::text OR usertype = 'patient'::text)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "User" OWNER TO postgres;


-- Table: "Sickness"

-- DROP TABLE "Sickness";

CREATE TABLE "Sickness"
(
  "name" text NOT NULL,
  CONSTRAINT "Sickness_pkey" PRIMARY KEY (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Sickness" OWNER TO postgres;


-- Table: "Symptom"

-- DROP TABLE "Symptom";

CREATE TABLE "Symptom"
(
  "name" text NOT NULL,
  img bytea,
  CONSTRAINT "Symptom_pkey" PRIMARY KEY (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Symptom" OWNER TO postgres;


-- Table: "Hospital"

-- DROP TABLE "Hospital";

CREATE TABLE "Hospital"
(
  "name" text NOT NULL,
  address text NOT NULL,
  coordinates point NOT NULL,
  CONSTRAINT "Hospital_pkey" PRIMARY KEY (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Hospital" OWNER TO postgres;


-- Table: "Diagnosis"

-- DROP TABLE "Diagnosis";

CREATE TABLE "Diagnosis"
(
  sickness text NOT NULL,
  symptom text NOT NULL,
  CONSTRAINT "Diagnosis_pkey" PRIMARY KEY (sickness, symptom),
  CONSTRAINT "SicknessFK" FOREIGN KEY (sickness)
      REFERENCES "Sickness" ("name") MATCH FULL
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "SymptomFK" FOREIGN KEY (symptom)
      REFERENCES "Symptom" ("name") MATCH FULL
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Diagnosis" OWNER TO postgres;


-- Table: "Curing"

-- DROP TABLE "Curing";

CREATE TABLE "Curing"
(
  sickness text NOT NULL,
  hospital text NOT NULL,
  CONSTRAINT "Curing_pkey" PRIMARY KEY (sickness, hospital),
  CONSTRAINT "HospitalFK" FOREIGN KEY (hospital)
      REFERENCES "Hospital" ("name") MATCH FULL
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "SicknessFK" FOREIGN KEY (sickness)
      REFERENCES "Sickness" ("name") MATCH FULL
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Curing" OWNER TO postgres;


-- Table: "PatientHealth"

-- DROP TABLE "PatientHealth";

CREATE TABLE "PatientHealth"
(
  username text NOT NULL,
  date timestamp without time zone NOT NULL,
  mood integer NOT NULL,
  weight double precision NOT NULL, -- Weight in kg
  temperature double precision NOT NULL, -- Body temperature in Celsius
  bloodpressure1 integer NOT NULL, -- Systoles pressure
  bloodpressure2 integer NOT NULL, -- Diastoles pressure
  pulse integer NOT NULL,
  CONSTRAINT "PatientInfo_PK" PRIMARY KEY (username, date),
  CONSTRAINT "User_FK" FOREIGN KEY (username)
      REFERENCES "User" (username) MATCH FULL
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "PatientHealth" OWNER TO postgres;
COMMENT ON COLUMN "PatientHealth".weight IS 'Weight in kg';
COMMENT ON COLUMN "PatientHealth".temperature IS 'Body temperature in Celsius';
COMMENT ON COLUMN "PatientHealth".bloodpressure1 IS 'Systoles pressure';
COMMENT ON COLUMN "PatientHealth".bloodpressure2 IS 'Diastoles pressure';


-- Table: "Picture"

-- DROP TABLE "Picture";

CREATE TABLE "Picture"
(
  imgname text NOT NULL,
  username text NOT NULL,
  img bytea,
  answered boolean,
  CONSTRAINT "Picture_pkey" PRIMARY KEY (imgname),
  CONSTRAINT "Picture_username_fkey" FOREIGN KEY (username)
      REFERENCES "User" (username) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Picture" OWNER TO postgres;


-- Table: "PictureComment"

-- DROP TABLE "PictureComment";

CREATE TABLE "PictureComment"
(
  imgname text NOT NULL,
  "comment" text,
  answer text,
  CONSTRAINT "PictureComment_pkey" PRIMARY KEY (imgname),
  CONSTRAINT "PictureComment_imgname_fkey" FOREIGN KEY (imgname)
      REFERENCES "Picture" (imgname) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "PictureComment" OWNER TO postgres;
