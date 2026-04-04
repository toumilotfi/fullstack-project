--
-- PostgreSQL database dump
--

\restrict doRFwpBIwDvxF1aMkb3ArvUnqEVAUgnzde3k4sj6dlpyqQ5hAXfke1eK5k733iQ

-- Dumped from database version 17.9 (Debian 17.9-1.pgdg13+1)
-- Dumped by pg_dump version 17.9 (Debian 17.9-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: lotfi
--

COPY public.users (id, user_a, created_at, email, f_name, l_name, password) FROM stdin;
5	t	2026-03-26 12:04:13.805001	lotfitoumi5@gmail.com	lotfi	toumi	123456
9	f	2026-04-03 00:10:14.846669	lotfitumi5@gmail.com	lotfi	toumi	tghtuiu
10	f	2026-04-03 01:55:43.647943	lotfitjumi5@gmail.com	lotfi	toumi	hkhlk
12	f	2026-04-03 01:55:54.858974	lotfitjulmi5@gmail.com	lotfi	toumi	hkhlk
6	t	2026-03-26 12:38:55.301133	lotfitoumi56@gmail.com	lotfi	toumi	123456
\.


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: lotfi
--

SELECT pg_catalog.setval('public.users_id_seq', 12, true);

--
-- Role migration: assign roles to existing users
-- (role column was added in microservices migration, did not exist in monolith)
--

UPDATE public.users SET role = 'USER'  WHERE role IS NULL;
UPDATE public.users SET role = 'ADMIN' WHERE id = 6;


--
-- PostgreSQL database dump complete
--

\unrestrict doRFwpBIwDvxF1aMkb3ArvUnqEVAUgnzde3k4sj6dlpyqQ5hAXfke1eK5k733iQ

