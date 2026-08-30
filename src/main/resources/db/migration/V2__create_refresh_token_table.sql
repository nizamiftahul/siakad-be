CREATE TABLE public."RefreshToken" (
    id SERIAL PRIMARY KEY,
    "userId" INTEGER NOT NULL REFERENCES public."User"(id) ON DELETE CASCADE,
    token TEXT NOT NULL,
    "expiresAt" TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    "createdAt" TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX "RefreshToken_token_unique" ON public."RefreshToken" (token);
CREATE INDEX "RefreshToken_userId_idx" ON public."RefreshToken" ("userId");