INSERT INTO owners (name, birthdate, password, role)
SELECT 'boss', '1990-01-15', '$2a$12$JNR4LEIEn11ZTLF23QUlLu9NlaU4wEsxtzEGc4Wn7XQtD5fjm78Ye', 'ADMIN'
    WHERE NOT EXISTS (SELECT 1 FROM owners WHERE role = 'ADMIN');