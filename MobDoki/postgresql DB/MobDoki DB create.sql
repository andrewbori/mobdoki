-- Table: "UserType"

-- DROP TABLE "UserType";

CREATE TABLE "UserType"
(
  id serial NOT NULL,
  "name" text NOT NULL,
  CONSTRAINT "UserType_PK" PRIMARY KEY (id),
  CONSTRAINT "Name_Unique" UNIQUE (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "UserType" OWNER TO postgres;

INSERT INTO "UserType" (name) VALUES ('doctor');
INSERT INTO "UserType" (name) VALUES ('patient');

-- Table: "Image"

-- DROP TABLE "Image";

CREATE TABLE "Image"
(
  id serial NOT NULL,
  image bytea NOT NULL,
  medium bytea,
  small bytea,
  CONSTRAINT "Image_PK" PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Image" OWNER TO postgres;


-- Table: "User"

-- DROP TABLE "User";

CREATE TABLE "User"
(
  id serial NOT NULL,
  username text NOT NULL,
  "password" integer NOT NULL,
  "usertypeID" integer NOT NULL,
  address text,
  "name" text,
  email text,
  "imageID" integer,
  CONSTRAINT "User_PK" PRIMARY KEY (id),
  CONSTRAINT "Image_FK" FOREIGN KEY ("imageID")
      REFERENCES "Image" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "UserType_FK" FOREIGN KEY ("usertypeID")
      REFERENCES "UserType" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "UserName_Unique" UNIQUE (username)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "User" OWNER TO postgres;


-- Table: "Message"

-- DROP TABLE "Message";

CREATE TABLE "Message"
(
  id serial NOT NULL,
  sender integer NOT NULL,
  recipient integer,
  date timestamp without time zone NOT NULL DEFAULT now(),
  subject text NOT NULL,
  "text" text NOT NULL,
  answered boolean NOT NULL DEFAULT false,
  viewed boolean NOT NULL DEFAULT false,
  "imageID" integer,
  CONSTRAINT "Message_PK" PRIMARY KEY (id),
  CONSTRAINT "Image_FK" FOREIGN KEY ("imageID")
      REFERENCES "Image" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "Recipient_FK" FOREIGN KEY (recipient)
      REFERENCES "User" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "Sender_FK" FOREIGN KEY (sender)
      REFERENCES "User" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Message" OWNER TO postgres;


-- Table: "Sickness"

-- DROP TABLE "Sickness";

CREATE TABLE "Sickness"
(
  id serial NOT NULL,
  "name" text NOT NULL,
  seriousness double precision NOT NULL DEFAULT 0.0,
  url text,
  CONSTRAINT "Sickness_PK" PRIMARY KEY (id),
  CONSTRAINT "Sickness_Name_Unique" UNIQUE (name),
  CONSTRAINT "Seriousness_Check" CHECK (seriousness >= 0.0::double precision AND seriousness <= 5.0::double precision)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Sickness" OWNER TO postgres;


-- Table: "Symptom"

-- DROP TABLE "Symptom";

CREATE TABLE "Symptom"
(
  id serial NOT NULL,
  "name" text NOT NULL,
  "imageID" integer,
  CONSTRAINT "Symptom_PK" PRIMARY KEY (id),
  CONSTRAINT "Image_FK" FOREIGN KEY ("imageID")
      REFERENCES "Image" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "Symptom_Name_Unique" UNIQUE (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Symptom" OWNER TO postgres;


-- Table: "Hospital"

-- DROP TABLE "Hospital";

CREATE TABLE "Hospital"
(
  id serial NOT NULL,
  "name" text NOT NULL,
  address text NOT NULL,
  coordinates point NOT NULL,
  CONSTRAINT "Hospital_PK" PRIMARY KEY (id),
  CONSTRAINT "Hospital_Unique" UNIQUE (name, address)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Hospital" OWNER TO postgres;


-- Table: "Diagnosis"

-- DROP TABLE "Diagnosis";

CREATE TABLE "Diagnosis"
(
  id serial NOT NULL,
  "sicknessID" integer NOT NULL,
  "symptomID" integer NOT NULL,
  CONSTRAINT "Diagnosis_PK" PRIMARY KEY (id),
  CONSTRAINT "Sickness_FK" FOREIGN KEY ("sicknessID")
      REFERENCES "Sickness" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "Symptom_FK" FOREIGN KEY ("symptomID")
      REFERENCES "Symptom" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "Diagnosis_Unique" UNIQUE ("sicknessID", "symptomID")
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Diagnosis" OWNER TO postgres;


-- Table: "Curing"

-- DROP TABLE "Curing";

CREATE TABLE "Curing"
(
  id serial NOT NULL,
  "hospitalID" integer NOT NULL,
  "sicknessID" integer NOT NULL,
  CONSTRAINT "Curing_PK" PRIMARY KEY (id),
  CONSTRAINT "Hospital_FK" FOREIGN KEY ("hospitalID")
      REFERENCES "Hospital" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "Sickness_FK" FOREIGN KEY ("sicknessID")
      REFERENCES "Sickness" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "Curing_Unique" UNIQUE ("hospitalID", "sicknessID")
)
WITH (
  OIDS=FALSE
);
ALTER TABLE "Curing" OWNER TO postgres;


-- Table: "PatientHealth"

-- DROP TABLE "PatientHealth";

CREATE TABLE "PatientHealth"
(
  id bigserial NOT NULL,
  "userID" integer NOT NULL,
  date timestamp without time zone NOT NULL DEFAULT now(),
  mood integer NOT NULL,
  weight double precision NOT NULL, -- Weight in kg
  temperature double precision NOT NULL, -- Body temperature in Celsius
  bloodpressure1 integer NOT NULL, -- Systoles pressure
  bloodpressure2 integer NOT NULL, -- Diastoles pressure
  pulse integer NOT NULL,
  CONSTRAINT "UserHealt_PK" PRIMARY KEY (id),
  CONSTRAINT "UserName_FK" FOREIGN KEY ("userID")
      REFERENCES "User" (id) MATCH SIMPLE
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