# Deployment — AWS EC2

All assignments run on **one** EC2 instance, `progweb-server` (t3.small, us-east-1), with Docker Compose.
We update that same instance; we never create new ones.

```
GitHub (main) ──► GitHub Actions "Deploy to EC2" (manual) ──SSH──► progweb-server
                                                                    └─ docker compose up -d --build
                                                                       ├─ gateway (Nginx, port 80)
                                                                       ├─ page1   (8080)
                                                                       ├─ page2   (8081)
                                                                       └─ db      (PostgreSQL, not public)
```

## Keeping costs at zero when not in use

- The instance is **stopped** when I'm not working. A stopped t3.small does not charge compute (the EBS disk still costs a few cents).
- The deploy workflow only runs when I press **Run workflow**. Nothing starts the instance automatically.
- No API Gateway, Load Balancer or NAT: those charge per hour/request. Nginx inside Docker does the gateway job for free.
- **Elastic IP:** free while attached to a *running* instance, about US$3.6/month while the instance is stopped.
  Without it, the public IP changes every time the instance starts, and the `EC2_HOST` secret must be updated.

## One-time setup

### 1. On the server (SSH from my PC)

```bash
# only the first time: clone the monorepo in the home folder
git clone https://github.com/jhon-avilat/web-programming-projects.git
cd web-programming-projects/assignments/01-product-catalog
cp .env.example .env      # put the real DB_PASSWORD (the same one Postgres already uses)
docker compose up -d --build
```

> **Moving from the old setup (keep the data).** The compose project is now always called `progweb`,
> so the database volume is `progweb_pgdata`. The old setup used a volume named after its folder
> (for example `web-programming-projects_pgdata`). Before the first `up`:
>
> ```bash
> docker compose -f <old-folder>/docker-compose.yml down        # NO -v: keeps the old volume
> docker volume ls                                               # find the old *_pgdata volume
> docker volume create progweb_pgdata
> docker run --rm -v <old>_pgdata:/from -v progweb_pgdata:/to alpine sh -c "cp -a /from/. /to/"
> ```
>
> If the old data doesn't matter, skip this: Postgres starts empty and `products_seed.sql` fills it again.

### 2. GitHub secrets (repo → Settings → Secrets and variables → Actions)

| Name | Type | Value |
|------|------|-------|
| `EC2_HOST` | secret | Public IP or DNS of the instance |
| `EC2_USER` | secret | `ubuntu` (Ubuntu) or `ec2-user` (Amazon Linux) |
| `EC2_SSH_KEY` | secret | Content of the `.pem` key |
| `EC2_APP_DIR` | variable (optional) | Folder of the repo on the server, default `web-programming-projects` |

### 3. Let GitHub reach port 22

The security group only allows SSH from my home IP. GitHub runners use changing IPs, so pick one:

- **Recommended:** create an IAM role for GitHub OIDC that can only call
  `ec2:AuthorizeSecurityGroupIngress` / `ec2:RevokeSecurityGroupIngress` on `launch-wizard-1`,
  then set the variables `AWS_ROLE_ARN` and `EC2_SECURITY_GROUP_ID`. The workflow opens port 22 for the
  runner's IP during the deploy and closes it again. IAM and OIDC have no cost.
- **Simplest (less secure):** allow port 22 from `0.0.0.0/0` (key-only login). Not recommended.

### 4. Gateway port

Open **port 80** (HTTP) from `0.0.0.0/0` in `launch-wizard-1` to use the Nginx gateway.
Once everything works through port 80, ports 8080/8081 can be closed.

## Deploying

1. Start the instance in the EC2 console (and update `EC2_HOST` if there is no Elastic IP).
2. GitHub → **Actions** → *Assignment 01 · Deploy to EC2* → **Run workflow** (branch `main`).
3. Open `http://<EC2_HOST>/`.
4. Stop the instance when finished.
