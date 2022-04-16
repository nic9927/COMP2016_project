CREATE TABLE Students(
	sno CHAR(10),
	name CHAR(20),
	gender CHAR(1),
	major CHAR(20),
PRIMARY KEY (sno));

CREATE TABLE Books(
	call_no CHAR(10),
	ISBN CHAR(10),
	title CHAR(20),
	author CHAR(20),
	amount INTEGER,
	location CHAR(10),
	UNIQUE (call_no),
	PRIMARY KEY (ISBN));



(Many to Many Relationships?)
CREATE TABLE Borrow(
	b_date DATE,
	d_date DATE,
	book CHAR(20),
	borrower CHAR(10),
	PRIMARY KEY (book, borrower),
	FOREIGN KEY (book) REFERENCES Books(call_no),
	FOREIGN KEY (borrower) REFERENCES Students(sno));

(One to Many Relationships?)
CREATE TABLE Reserve(
	sno CHAR(10),
	book CHAR(20),
	reserveDate DATE
	PRIMARY KEY (sno),
FOREIGN KEY (sno) REFERENCES Student,
	FOREIGN KEY (book) REFERENCES Books(call_no));

CREATE TABLE Renew(
	sno CHAR(10),
	Book CHAR(20),
	PRIMARY KEY (sno, book)
	FOREIGN KEY (sno) REFERENCES Student,
	FOREIGN KEY (book) REFERENCES Books(call_no));

COMMIT;

CREATE OR REPLACE TRIGGER Borrow_constraint
BEFORE INSERT OR UPDATE ON Borrow
FOR EACH ROW
BEGIN
IF (:NEW.b_date > :NEW.d_date) THEN
RAISE_APPLICATION_ERROR(-20000, ‘Invalid date’);
END IF;
END;
/

(Count the amount of books student has borrow)
CREATE OR REPLACE TRIGGER Borrow_INSERT
AFTER INSERT ON Borrow
FOR EACH ROW
DECLARE
c INTEGER;
BEGIN
SELECT COUNT(*) into c FROM Students
WHERE sno = :NEW.sno;
IF(c = 0) THEN
INSERT INTO Students VALUES(:NEW.sno, 1);
ELSE
UPDATE Students SET bor_cnt = bor_cnt + 1
WHERE sno = :NEW.sno;
END IF;
END;
/

(Return book amount)
CREATE OR REPLACE TRIGGER Borrow_DELETE
AFTER DELETE ON Borrow
FOR EACH ROW
DECLARE
c INTEGER;
BEGIN
SELECT bor_cnt into c FROM Students
WHERE sno = :OLD.sno;
IF(c = 1) THEN
DELETE FROM Students
WHERE sno = :OLD.sno;
ELSE
UPDATE Students SET bor_cnt = bor_cnt -1
WHERE sno = :OLD.sno;
END IF;
END;
/

COMMIT;
