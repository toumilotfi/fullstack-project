--
-- PostgreSQL database dump
--

\restrict GjVJEBw40z30m6cmxaiQVbV27duvmjvbQIxQFtzXCLTxZaoDwmEdzhrBPRo5Led

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
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: lotfi
--

COPY public.notifications (id, is_read, user_id, created_at, message, title) FROM stdin;
4	t	4	2026-03-23 23:18:30.895333	vjj	Admin Message
3	t	4	2026-03-23 23:18:21.759105	hi	Admin Message
2	t	4	2026-03-23 23:17:03.312347	You have been assigned a new task: gkjhl	New Task Assigned
1	t	4	2026-03-23 23:16:50.056135	You have been assigned a new task: hi	New Task Assigned
5	t	4	2026-03-23 23:57:23.359865	hi	Admin Message
6	t	4	2026-03-24 00:08:17.629398	hh	Admin Message
10	t	4	2026-03-24 00:08:48.094048	hh	Admin Message
9	t	4	2026-03-24 00:08:47.92456	hh	Admin Message
8	t	4	2026-03-24 00:08:47.732591	hh	Admin Message
7	t	4	2026-03-24 00:08:47.558187	hh	Admin Message
11	t	4	2026-03-24 00:12:41.197305	hh	Admin Message
12	f	1	2026-03-24 00:18:35.010592	User responded to task: hi	Task Response Received
13	f	4	2026-03-26 12:24:03.824085	hi	Admin Message
14	f	5	2026-03-26 12:24:03.836321	hi	Admin Message
15	f	5	2026-03-26 12:40:48.307577	You have been assigned a new task: first test 	New Task Assigned
17	f	1	2026-03-26 12:42:06.960409	User responded to task: hi	Task Response Received
19	f	4	2026-03-26 12:43:23.268259	Your task has been approved: hi	Task Approved
18	t	6	2026-03-26 12:43:22.161144	Your task has been approved: hi	Task Approved
16	t	6	2026-03-26 12:41:12.89143	You have been assigned a new task: hi	New Task Assigned
20	t	6	2026-04-04 10:23:51.073296	You have been assigned a new task: h	New Task Assigned
21	f	1	2026-04-04 10:24:24.36778	User responded to task: h	Task Response Received
22	f	6	2026-04-04 10:24:43.417558	Your task has been approved: h	Task Approved
\.


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: lotfi
--

SELECT pg_catalog.setval('public.notifications_id_seq', 22, true);


--
-- PostgreSQL database dump complete
--

\unrestrict GjVJEBw40z30m6cmxaiQVbV27duvmjvbQIxQFtzXCLTxZaoDwmEdzhrBPRo5Led

