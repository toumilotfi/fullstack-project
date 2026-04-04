--
-- PostgreSQL database dump
--

\restrict 5O3ONmdNbjw2V152uJm58Yv1JLm6YLiLpk64r4VLMqq8ooNdWY14uSEhkQ0ZYAQ

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
-- Data for Name: tasks; Type: TABLE DATA; Schema: public; Owner: lotfi
--

COPY public.tasks (assigned_to_user_id, id, created_at, response_at, description, user_response, status, title) FROM stdin;
4	2	2026-03-23 23:17:03.302439	\N	gjhgjkh	\N	ASSIGNED	gkjhl
5	3	2026-03-26 12:40:48.291632	\N	fenich the controler	\N	ASSIGNED	first test 
6	4	2026-03-26 12:41:12.882023	2026-03-26 12:42:06.952763	fenich this 	{"response":"ok"}	APPROVED	hi
4	1	2026-03-23 23:16:50.042547	2026-03-24 00:18:34.998699	hiii	{"response":"okay"}	APPROVED	hi
6	5	2026-04-04 10:23:51.009294	2026-04-04 10:24:24.360367	hhh	{"response":"h"}	APPROVED	h
\.


--
-- Name: tasks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: lotfi
--

SELECT pg_catalog.setval('public.tasks_id_seq', 5, true);


--
-- PostgreSQL database dump complete
--

\unrestrict 5O3ONmdNbjw2V152uJm58Yv1JLm6YLiLpk64r4VLMqq8ooNdWY14uSEhkQ0ZYAQ

