DROP TABLE ARTIST;
DROP TABLE GALLERY;
DROP TABLE PAINTING; 
DROP TABLE AUTO_PK_SUPPORT;

CREATE TABLE ARTIST (
	DATE_OF_BIRTH DATE NULL,
	ARTIST_ID INT NOT NULL, 
	ARTIST_NAME CHAR(255) NOT NULL, 
	PRIMARY KEY (ARTIST_ID)
);

CREATE TABLE GALLERY (
	GALLERY_ID INT NOT NULL, 
	GALLERY_NAME VARCHAR(100) NOT NULL, 
	PRIMARY KEY (GALLERY_ID)
);

CREATE TABLE PAINTING (
	PAINTING_TITLE VARCHAR(255) NOT NULL, 
	GALLERY_ID INT NULL, 
	ESTIMATED_PRICE DECIMAL NULL, 
	PAINTING_ID INT NOT NULL, 
	ARTIST_ID INT NULL, 
	PRIMARY KEY (PAINTING_ID)
);

INSERT INTO GALLERY (GALLERY_ID, GALLERY_NAME) 
VALUES (1, 'The Metropolitan Museum of Art');

INSERT INTO GALLERY (GALLERY_ID, GALLERY_NAME) 
VALUES (2, 'Louvre');

INSERT INTO GALLERY (GALLERY_ID, GALLERY_NAME) 
VALUES (3, 'The State Hermitage Museum');


INSERT INTO ARTIST (DATE_OF_BIRTH, ARTIST_ID, ARTIST_NAME) 
VALUES ('1970-06-05', 1, 'Andrus Adamchik');

INSERT INTO ARTIST (DATE_OF_BIRTH, ARTIST_ID, ARTIST_NAME) 
VALUES ('1969-07-08', 2, 'Matt Kerr');

INSERT INTO ARTIST (DATE_OF_BIRTH, ARTIST_ID, ARTIST_NAME) 
VALUES ('1972-07-23', 3, 'Eric Schneider');

INSERT INTO ARTIST (DATE_OF_BIRTH, ARTIST_ID, ARTIST_NAME) 
VALUES ('1967-04-25', 4, 'Kyle Dawkins');

INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (1, 1, 1, 'Andrus Painting One', 2500);

INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (2, 1, 2, 'Andrus Painting Two', 3000);

INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (3, 1, 3, 'Andrus Painting Three', 3500);


INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (4, 2, 1, 'Matt Painting One', 2000);

INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (5, 2, 2, 'Matt Painting Two', 2300);

INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (6, 2, NULL, 'Matt Painting Three', 3200);


INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (7, 3, 1, 'Eric Painting One', 2600);

INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (8, 3, NULL, 'Eric Painting Two', 2300);

INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (9, 3, 3, 'Eric Painting Three', 3100);


INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (10, 4, NULL, 'Kyle Painting One', 2200);

INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (11, 4, 2, 'Kyle Painting Two', 2800);

INSERT INTO PAINTING (PAINTING_ID, ARTIST_ID, GALLERY_ID, PAINTING_TITLE, ESTIMATED_PRICE) 
VALUES (12, 4, 3, 'Kyle Painting Three', 1600);
