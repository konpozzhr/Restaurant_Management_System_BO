/*
User 
 Admin: admin123
 projectuser: SathyaKK
 
*/

CREATE DATABASE project_management;
CREATE USER pm_user WITH PASSWORD 'yourStrongPassword';
GRANT ALL PRIVILEGES ON DATABASE project_management TO pm_user;




+ Import database 

\i C:/PostgreSQL/postgres_import.sql


/*
Good news: there's a fix that doesn't require reinstalling PostgreSQL — use the C locale, which is encoding-agnostic and lets you force UTF8 regardless of what template0's locale is:
*/

\c postgres
DROP DATABASE project_management;
CREATE DATABASE project_management 
    WITH ENCODING 'UTF8' 
    LC_COLLATE 'C' 
    LC_CTYPE 'C' 
    TEMPLATE template0;
GRANT ALL PRIVILEGES ON DATABASE project_management TO projectuser;


--Then verify:
\c project_management
SELECT current_database(), pg_encoding_to_char(encoding), datcollate, datctype 
FROM pg_database WHERE datname = current_database();


-- Fix — grant the privilege
psql -U postgres -h localhost -d project_management

GRANT USAGE, CREATE ON SCHEMA public TO projectuser;

ALTER SCHEMA public OWNER TO projectuser;


-- rerun app and verify 

\c project_management
\dt

-- Import data again 
\i C:/PostgreSQL/postgres_import.sql