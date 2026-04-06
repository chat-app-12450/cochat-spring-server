--
-- PostgreSQL database dump
--

-- Dumped from database version 16.4 (Debian 16.4-1.pgdg110+2)
-- Dumped by pg_dump version 16.4 (Debian 16.4-1.pgdg110+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry and geography spatial types and functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: chat_bot_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_bot_message (
    id bigint NOT NULL,
    content text NOT NULL,
    "timestamp" timestamp(6) without time zone NOT NULL,
    type character varying(255) NOT NULL,
    user_id bigint NOT NULL,
    chat_room_id bigint NOT NULL
);


--
-- Name: chat_bot_message_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chat_bot_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: chat_bot_message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.chat_bot_message_id_seq OWNED BY public.chat_bot_message.id;


--
-- Name: chat_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_message (
    id bigint NOT NULL,
    message character varying(255) NOT NULL,
    message_seq bigint NOT NULL,
    received_at timestamp(6) without time zone NOT NULL,
    chat_room_id bigint,
    sender_id bigint
);


--
-- Name: chat_message_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chat_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: chat_message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.chat_message_id_seq OWNED BY public.chat_message.id;


--
-- Name: chat_participant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_participant (
    id bigint NOT NULL,
    join_seq bigint NOT NULL,
    last_read_seq bigint NOT NULL,
    leave_seq bigint,
    chat_room_id bigint NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: chat_participant_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chat_participant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: chat_participant_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.chat_participant_id_seq OWNED BY public.chat_participant.id;


--
-- Name: chat_read_status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_read_status (
    id bigint NOT NULL,
    last_read_seq bigint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    chat_room_id bigint,
    user_id bigint
);


--
-- Name: chat_read_status_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chat_read_status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: chat_read_status_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.chat_read_status_id_seq OWNED BY public.chat_read_status.id;


--
-- Name: chat_room; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_room (
    id bigint NOT NULL,
    chat_room_type character varying(255),
    description character varying(500),
    last_message_seq bigint NOT NULL,
    latest_message_at timestamp(6) without time zone,
    latest_message_id bigint,
    latitude double precision,
    location public.geography(Point,4326),
    location_label character varying(120),
    longitude double precision,
    max_participants integer,
    name character varying(255) NOT NULL,
    open_chat boolean NOT NULL,
    creator_id bigint NOT NULL,
    product_id bigint
);


--
-- Name: chat_room_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chat_room_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: chat_room_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.chat_room_id_seq OWNED BY public.chat_room.id;


--
-- Name: follow; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.follow (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    following_id bigint NOT NULL,
    follower_id bigint NOT NULL
);


--
-- Name: follow_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.follow_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: follow_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.follow_id_seq OWNED BY public.follow.id;


--
-- Name: notification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    notification_content_id bigint NOT NULL,
    receiver_id bigint NOT NULL
);


--
-- Name: notification_content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification_content (
    id bigint NOT NULL,
    message character varying(255) NOT NULL
);


--
-- Name: notification_content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notification_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notification_content_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notification_content_id_seq OWNED BY public.notification_content.id;


--
-- Name: notification_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notification_id_seq OWNED BY public.notification.id;


--
-- Name: outbox_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.outbox_event (
    id bigint NOT NULL,
    aggregate_id bigint NOT NULL,
    aggregate_type character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    event_key character varying(100) NOT NULL,
    event_type character varying(50) NOT NULL,
    payload text NOT NULL,
    published_at timestamp(6) without time zone,
    retry_count integer NOT NULL,
    status character varying(20) NOT NULL,
    topic character varying(200) NOT NULL
);


--
-- Name: outbox_event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.outbox_event_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: outbox_event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.outbox_event_id_seq OWNED BY public.outbox_event.id;


--
-- Name: product_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_images (
    id bigint NOT NULL,
    image_url character varying(2048) NOT NULL,
    sort_order integer NOT NULL,
    product_id bigint NOT NULL
);


--
-- Name: product_images_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: product_images_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_images_id_seq OWNED BY public.product_images.id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    description character varying(2000) NOT NULL,
    latitude double precision,
    location public.geography(Point,4326),
    location_label character varying(120),
    longitude double precision,
    price bigint NOT NULL,
    status character varying(20) NOT NULL,
    title character varying(100) NOT NULL,
    version bigint NOT NULL,
    reserved_buyer_id bigint,
    seller_id bigint NOT NULL,
    sold_buyer_id bigint
);


--
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;


--
-- Name: user_verified_location; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_verified_location (
    id bigint NOT NULL,
    latitude double precision NOT NULL,
    location public.geography(Point,4326),
    location_label character varying(120),
    longitude double precision NOT NULL,
    verified_at timestamp(6) without time zone NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: user_verified_location_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_verified_location_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_verified_location_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_verified_location_id_seq OWNED BY public.user_verified_location.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    profile_image_url character varying(255),
    user_id character varying(255) NOT NULL
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: chat_bot_message id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_bot_message ALTER COLUMN id SET DEFAULT nextval('public.chat_bot_message_id_seq'::regclass);


--
-- Name: chat_message id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_message ALTER COLUMN id SET DEFAULT nextval('public.chat_message_id_seq'::regclass);


--
-- Name: chat_participant id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_participant ALTER COLUMN id SET DEFAULT nextval('public.chat_participant_id_seq'::regclass);


--
-- Name: chat_read_status id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_read_status ALTER COLUMN id SET DEFAULT nextval('public.chat_read_status_id_seq'::regclass);


--
-- Name: chat_room id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_room ALTER COLUMN id SET DEFAULT nextval('public.chat_room_id_seq'::regclass);


--
-- Name: follow id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follow ALTER COLUMN id SET DEFAULT nextval('public.follow_id_seq'::regclass);


--
-- Name: notification id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification ALTER COLUMN id SET DEFAULT nextval('public.notification_id_seq'::regclass);


--
-- Name: notification_content id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification_content ALTER COLUMN id SET DEFAULT nextval('public.notification_content_id_seq'::regclass);


--
-- Name: outbox_event id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outbox_event ALTER COLUMN id SET DEFAULT nextval('public.outbox_event_id_seq'::regclass);


--
-- Name: product_images id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_images ALTER COLUMN id SET DEFAULT nextval('public.product_images_id_seq'::regclass);


--
-- Name: products id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);


--
-- Name: user_verified_location id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_verified_location ALTER COLUMN id SET DEFAULT nextval('public.user_verified_location_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: chat_bot_message chat_bot_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_bot_message
    ADD CONSTRAINT chat_bot_message_pkey PRIMARY KEY (id);


--
-- Name: chat_message chat_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_message
    ADD CONSTRAINT chat_message_pkey PRIMARY KEY (id);


--
-- Name: chat_participant chat_participant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_participant
    ADD CONSTRAINT chat_participant_pkey PRIMARY KEY (id);


--
-- Name: chat_read_status chat_read_status_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_read_status
    ADD CONSTRAINT chat_read_status_pkey PRIMARY KEY (id);


--
-- Name: chat_room chat_room_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_room
    ADD CONSTRAINT chat_room_pkey PRIMARY KEY (id);


--
-- Name: follow follow_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follow
    ADD CONSTRAINT follow_pkey PRIMARY KEY (id);


--
-- Name: notification_content notification_content_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification_content
    ADD CONSTRAINT notification_content_pkey PRIMARY KEY (id);


--
-- Name: notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);


--
-- Name: outbox_event outbox_event_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outbox_event
    ADD CONSTRAINT outbox_event_pkey PRIMARY KEY (id);


--
-- Name: product_images product_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT product_images_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: user_verified_location uk_user_verified_location_user; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_verified_location
    ADD CONSTRAINT uk_user_verified_location_user UNIQUE (user_id);


--
-- Name: user_verified_location user_verified_location_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_verified_location
    ADD CONSTRAINT user_verified_location_pkey PRIMARY KEY (id);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: users uk_6efs5vmce86ymf5q7lmvn2uuf; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6efs5vmce86ymf5q7lmvn2uuf UNIQUE (user_id);


--
-- Name: chat_read_status uk_chat_read_status_user_room; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_read_status
    ADD CONSTRAINT uk_chat_read_status_user_room UNIQUE (user_id, chat_room_id);


--
-- Name: follow ukfb7ln73htigy8q3cx7ebyho3c; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follow
    ADD CONSTRAINT ukfb7ln73htigy8q3cx7ebyho3c UNIQUE (follower_id, following_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_chat_message_room_id_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_message_room_id_id ON public.chat_message USING btree (chat_room_id, id);


--
-- Name: idx_chat_message_room_id_message_seq; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_message_room_id_message_seq ON public.chat_message USING btree (chat_room_id, message_seq);


--
-- Name: idx_chat_participant_room_leave_seq; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_participant_room_leave_seq ON public.chat_participant USING btree (chat_room_id, leave_seq);


--
-- Name: idx_chat_participant_room_user_leave_seq; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_participant_room_user_leave_seq ON public.chat_participant USING btree (chat_room_id, user_id, leave_seq);


--
-- Name: idx_chat_read_status_user_room; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_read_status_user_room ON public.chat_read_status USING btree (user_id, chat_room_id);


--
-- Name: idx_chat_room_last_message_seq; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_room_last_message_seq ON public.chat_room USING btree (last_message_seq);


--
-- Name: idx_chat_room_lat_lng; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_room_lat_lng ON public.chat_room USING btree (latitude, longitude);


--
-- Name: idx_chat_room_location_gist; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_room_location_gist ON public.chat_room USING gist (location);


--
-- Name: idx_chat_room_latest_message_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_room_latest_message_at ON public.chat_room USING btree (latest_message_at);


--
-- Name: idx_chat_room_open_chat; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_room_open_chat ON public.chat_room USING btree (open_chat);


--
-- Name: idx_chat_room_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_room_type ON public.chat_room USING btree (chat_room_type);


--
-- Name: idx_outbox_event_event_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_outbox_event_event_type ON public.outbox_event USING btree (event_type);


--
-- Name: idx_outbox_event_status_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_outbox_event_status_id ON public.outbox_event USING btree (status, id);


--
-- Name: idx_products_seller_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_seller_created_at ON public.products USING btree (seller_id, created_at);


--
-- Name: idx_products_status_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_status_created_at ON public.products USING btree (status, created_at);


--
-- Name: idx_products_lat_lng; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_lat_lng ON public.products USING btree (latitude, longitude);


--
-- Name: idx_products_location_gist; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_location_gist ON public.products USING gist (location);


--
-- Name: idx_user_verified_location_location_gist; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_verified_location_location_gist ON public.user_verified_location USING gist (location);


--
-- Name: idx_user_verified_location_verified_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_verified_location_verified_at ON public.user_verified_location USING btree (verified_at);


--
-- Name: chat_message fk5f82aoyy0jiwpj08qapfrxbh6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_message
    ADD CONSTRAINT fk5f82aoyy0jiwpj08qapfrxbh6 FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: follow fk9oqsjovu9bl95dwt8ibiy2oey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follow
    ADD CONSTRAINT fk9oqsjovu9bl95dwt8ibiy2oey FOREIGN KEY (following_id) REFERENCES public.users(id);


--
-- Name: products fkbgw3lyxhsml3kfqnfr45o0vbj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fkbgw3lyxhsml3kfqnfr45o0vbj FOREIGN KEY (seller_id) REFERENCES public.users(id);


--
-- Name: chat_read_status fkc0i41iogah5mcjpqbi1erc8m5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_read_status
    ADD CONSTRAINT fkc0i41iogah5mcjpqbi1erc8m5 FOREIGN KEY (chat_room_id) REFERENCES public.chat_room(id);


--
-- Name: notification fkdammjl0v5xfaegi926ugx6254; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT fkdammjl0v5xfaegi926ugx6254 FOREIGN KEY (receiver_id) REFERENCES public.users(id);


--
-- Name: chat_room fkdamy0nkn8t5fba717mb8tn62i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_room
    ADD CONSTRAINT fkdamy0nkn8t5fba717mb8tn62i FOREIGN KEY (creator_id) REFERENCES public.users(id);


--
-- Name: products fkdqh8efd3elbkqoh4hv1frakam; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fkdqh8efd3elbkqoh4hv1frakam FOREIGN KEY (reserved_buyer_id) REFERENCES public.users(id);


--
-- Name: chat_participant fke2s50kw19y5jwfi23jl5v6ov7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_participant
    ADD CONSTRAINT fke2s50kw19y5jwfi23jl5v6ov7 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: products fkedfrc35n12bk0mcebj1mkitup; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fkedfrc35n12bk0mcebj1mkitup FOREIGN KEY (sold_buyer_id) REFERENCES public.users(id);


--
-- Name: chat_message fkj52yap2xrm9u0721dct0tjor9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_message
    ADD CONSTRAINT fkj52yap2xrm9u0721dct0tjor9 FOREIGN KEY (chat_room_id) REFERENCES public.chat_room(id);


--
-- Name: follow fkjikg34txcxnhcky26w14fvfcc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.follow
    ADD CONSTRAINT fkjikg34txcxnhcky26w14fvfcc FOREIGN KEY (follower_id) REFERENCES public.users(id);


--
-- Name: chat_bot_message fkmrisoue8s7nj1w9dh480xvve2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_bot_message
    ADD CONSTRAINT fkmrisoue8s7nj1w9dh480xvve2 FOREIGN KEY (chat_room_id) REFERENCES public.chat_room(id);


--
-- Name: notification fkpprsmiog4jajk3asl0x1cqsfp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT fkpprsmiog4jajk3asl0x1cqsfp FOREIGN KEY (notification_content_id) REFERENCES public.notification_content(id);


--
-- Name: chat_participant fkqaqt420qk0puto2opt6st1u42; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_participant
    ADD CONSTRAINT fkqaqt420qk0puto2opt6st1u42 FOREIGN KEY (chat_room_id) REFERENCES public.chat_room(id);


--
-- Name: product_images fkqnq71xsohugpqwf3c9gxmsuy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_images
    ADD CONSTRAINT fkqnq71xsohugpqwf3c9gxmsuy FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: chat_read_status fkqrj508rpl4k59r3a4acg7e3t4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_read_status
    ADD CONSTRAINT fkqrj508rpl4k59r3a4acg7e3t4 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: chat_room fkrl6h4t5f5ro5mg3stqyruxm9o; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_room
    ADD CONSTRAINT fkrl6h4t5f5ro5mg3stqyruxm9o FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: user_verified_location fk_user_verified_location_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_verified_location
    ADD CONSTRAINT fk_user_verified_location_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--
