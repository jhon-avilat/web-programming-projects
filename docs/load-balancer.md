# Load balancer — AWS Application Load Balancer

Assignment 01 can run on **two instances** behind an **Application Load Balancer (ALB)**.
The ALB shares the traffic between them and stops sending requests to a node that fails its health check.

```
                         Internet
                            │  http://progweb-alb-....elb.amazonaws.com/
                            ▼
              ┌───────────────────────────────┐
              │  ALB  progweb-alb  (port 80)  │   2 zones: us-east-1c + us-east-1a
              │  target group progweb-tg      │   health check: GET /health every 10 s
              └──────────────┬────────────────┘   sticky sessions: 1 hour
                 ┌───────────┴────────────┐
                 ▼                        ▼
   progweb-server (us-east-1c)    progweb-app-node-2 (us-east-1a)
   NODE_NAME=progweb-1            NODE_NAME=progweb-2
   gateway + page1 + page2        gateway + page1 + page2
   PostgreSQL  ◄───── 5432 ────── (uses the primary's database)
```

## Why it is built this way

| Decision | Why |
|----------|-----|
| **One shared database** (on the primary) | If each node had its own PostgreSQL, a product saved on node 1 would not exist on node 2. Both nodes must read and write the same data. |
| **Second node made from an AMI** of `progweb-server` | The copy already has Docker, the repository and the `.env` with the database password, so no secret has to be typed or stored anywhere else. |
| **Role chosen in `.env`** (`COMPOSE_FILE`) | Same code on every node. `compose.primary.yml` shares PostgreSQL; `compose.app-node.yml` skips the database and uses `DB_HOST`. |
| **Health check on `/health`** | The Nginx gateway answers it without touching Java or the database, so it is cheap and fast. |
| **Sticky sessions** (ALB cookie) | Flash messages ("Product saved") are kept in the HTTP session of one node. Without stickiness, the redirect could land on the other node and the message would be lost. |
| **Two zones only** | An ALB needs at least two zones. Each extra zone adds a public IPv4 address billed by the hour. |
| **Security groups in a chain** | Internet → ALB (80) → nodes (80 only from the ALB) → PostgreSQL (5432 only from the app nodes). |
| **CloudFormation** (`infra/alb.yml`) | The whole setup is created and deleted with one command, so the ALB exists only while it is needed. |

## Cost (us-east-1, prices at the time of writing — check the AWS Pricing Calculator)

| Resource | Price | Note |
|----------|-------|------|
| ALB | ~US$0.0225 / hour + usage (LCU) | **Charged every hour it exists, even if the instances are stopped** |
| ALB public IPv4 (2 zones) | 2 × US$0.005 / hour | Same: billed while the ALB exists |
| Second node t3.small | ~US$0.0208 / hour | Only while it is running |
| Second node public IPv4 | US$0.005 / hour | Only while it is running |
| AMI snapshot | ~US$0.05 per GB-month | A few cents; delete it when it is no longer needed |

**Total while running:** about **US$0.06 per hour** on top of `progweb-server` (≈ US$1.4 per day).
Left on for a month it would be ≈ US$40, and ≈ US$24 a month even with both instances stopped (ALB + its IPs).
That's why the rule is: **create the stack for the demo, delete it afterwards.**

## One-time preparation

1. Start `progweb-server` and, in `assignments/01-product-catalog/.env`, add:
   ```
   COMPOSE_FILE=docker-compose.yml:compose.primary.yml
   NODE_NAME=progweb-1
   ```
   then `git pull` and `docker compose up -d --build`. PostgreSQL is now published on 5432
   (the security group only opens it to the app nodes once the stack exists).
2. EC2 → Instances → `progweb-server` → **Actions → Image and templates → Create image**,
   name `progweb-server-ami`. Wait until the AMI is **Available** and copy its id (`ami-...`).
   Create a new AMI whenever the code on the primary changes, or `git pull` on both nodes.

## Create the load balancer

Open **CloudShell** in the AWS console (free, it already has the AWS CLI) and run:

```bash
curl -sSO https://raw.githubusercontent.com/jhon-avilat/web-programming-projects/main/infra/alb.yml
aws cloudformation deploy --stack-name progweb-alb --template-file alb.yml \
  --parameter-overrides AppNodeAmiId=ami-xxxxxxxx SshCidr=$(curl -s https://checkip.amazonaws.com)/32
aws cloudformation describe-stacks --stack-name progweb-alb --query "Stacks[0].Outputs" --output table
```

> `SshCidr` must be **my** public IP. In CloudShell `checkip` returns CloudShell's IP, so replace it with
> the IP shown at https://checkip.amazonaws.com on my own PC.

The stack takes about 3–5 minutes. The second node needs a few more minutes to build the apps.

## Check that it balances

```bash
ALB=http://progweb-alb-xxxx.us-east-1.elb.amazonaws.com
for i in 1 2 3 4 5 6; do curl -s $ALB/health; done      # answers alternate: ok progweb-1 / ok progweb-2
curl -sI $ALB/page1/ | grep -i x-served-by              # which node served the page
```

- EC2 → **Target groups → progweb-tg → Targets**: both instances must be **healthy**.
- In the browser, `/page1/listarProductos` shows the same products on both nodes (same database).
- To see failover: stop the containers on one node (`docker compose stop gateway`); after ~30 s the
  target is unhealthy and every request goes to the other node.

## Presenting it (when the stack already exists)

Between demos the stack can stay created with **both instances stopped**: then only the ALB and its
two public IPs are billed (≈ US$0.03 per hour) and the ALB address doesn't change.

**Before the presentation (≈ 5 minutes):**

1. EC2 → Instances → start **`progweb-server` first** (it has the database).
2. When it is *Running*, start **`progweb-app-node-2`**. Its apps retry until the database answers.
3. EC2 → Target groups → `progweb-tg` → Targets: wait until both are **healthy**.
4. CloudFormation → `progweb-alb` → Outputs → open **AlbUrl**.

**Demo script:**

| Show | How |
|------|-----|
| One address for everything | Open `AlbUrl`, then `/page1/` and `/page2/page2/index.html` |
| The traffic is shared | In CloudShell: `for i in 1 2 3 4 5 6; do curl -s $ALB/health; done` → `ok progweb-1` / `ok progweb-2` |
| Same data on both nodes | `curl -sI $ALB/page1/api/productos \| grep -i x-served-by` a few times: different node, same products |
| Sticky sessions | In the browser, save a product: the green message appears, because the browser stays on one node (cookie `AWSALB`) |
| Failover | Stop `progweb-app-node-2` in the console; after ~20 s every answer is `ok progweb-1` and the pages keep working. Start it again and it rejoins by itself |

**After the presentation:** stop both instances again, or delete the stack (next section) if no more demos are planned.

## Delete it (stop paying for the ALB)

```bash
aws cloudformation delete-stack --stack-name progweb-alb
aws cloudformation wait stack-delete-complete --stack-name progweb-alb
```

This removes the ALB, the target group, the second node (terminated) and the extra security group rules.
`progweb-server` and its data are not touched. Optionally deregister the AMI and delete its snapshot.

## Limits

- **The database is still a single point of failure**: if `progweb-server` goes down, both nodes lose
  the database. The production answer is Amazon RDS Multi-AZ, which costs much more.
- **HTTP only.** HTTPS needs a domain and a certificate (AWS Certificate Manager is free, the domain is not).
- **SSH to the second node may time out with a /32 rule** if the internet provider uses a pool of public
  IPs (CGNAT): the IP seen by `checkip` is not always the one SSH leaves from. Use a /24 for `SshCidr`.
  Web traffic is not affected, it always goes through the ALB.
- **The deploy workflow updates one host.** With two nodes, run `git pull && docker compose up -d --build`
  on both, or create a new AMI and recreate the stack.
