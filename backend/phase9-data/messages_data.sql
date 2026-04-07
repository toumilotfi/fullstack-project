--
-- PostgreSQL database dump
--

\restrict kxsmH0zb6etrkKOcmYpWzoo8FVucDHgrjP7EdWszPdWp38Aah7QyBmMjoua1fzE

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
-- Data for Name: chat_messages; Type: TABLE DATA; Schema: public; Owner: lotfi
--

COPY public.chat_messages (id, is_read, receiver_id, sender_id, created_at, content, sender_role) FROM stdin;
1	f	4	1	2026-03-23 23:17:22.103142	hi	ADMIN
2	f	4	1	2026-03-23 23:17:39.321995	hi	ADMIN
3	f	4	1	2026-03-23 23:17:39.897548	hi	ADMIN
4	f	1	4	2026-03-23 23:17:54.152122	hey	USER
5	f	1	4	2026-03-24 00:25:28.078674	hey	USER
6	f	4	1	2026-03-26 12:24:23.016954	hi	ADMIN
7	f	1	6	2026-03-26 12:42:33.534008	hey admin	USER
8	f	6	1	2026-03-26 12:43:57.266076	hey yes tell me 	ADMIN
9	f	1	6	2026-04-03 00:08:21.211037	hi	USER
10	f	6	1	2026-04-03 00:08:35.399364	hi	ADMIN
\.


--
-- Name: chat_messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: lotfi
--

SELECT pg_catalog.setval('public.chat_messages_id_seq', 10, true);


--
-- PostgreSQL database dump complete
--

\unrestrict kxsmH0zb6etrkKOcmYpWzoo8FVucDHgrjP7EdWszPdWp38Aah7QyBmMjoua1fzE

