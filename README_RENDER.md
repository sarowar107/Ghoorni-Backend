Render deployment notes for Ghoorni-Backend

1. Recommended setup

- Create a new Web Service on Render using the Docker option (branch: main).
- Create a Managed Database on Render (Postgres recommended) or provide an external MySQL.

2. Required environment variables (in your Render service settings)

- SPRING_DATASOURCE_URL - jdbc URL for your DB. Examples:
  - MySQL: jdbc:mysql://<host>:3306/ghoorni?useSSL=false&serverTimezone=UTC
  - Postgres: jdbc:postgresql://<host>:5432/ghoorni
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- (optional) HIBERNATE_DIALECT - e.g., org.hibernate.dialect.PostgreSQLDialect

3. Port binding

- Render sets PORT automatically. The app reads it via server.port=${PORT:8080}.

4. Startup wait

- The container includes `entrypoint.sh` which will poll the DB host:port before launching the jar (30 retries, 2s delay).

5. Troubleshooting

- If you still get JDBC connection errors, verify:
  - The DB host/port are reachable from Render (network rules).
  - Credentials are correct.
  - For managed databases, use the connection string shown in Render's DB dashboard.

6. Quick deploy steps

- Commit & push changes (Dockerfile, entrypoint.sh, README_RENDER.md)
- On Render, click "Manual Deploy" -> "Clear build cache & deploy"

7. Optional improvements

- Use a health check route in the app and configure Render health checks.
- Use Render managed Postgres and change `spring.jpa.properties.hibernate.dialect` to PostgreSQLDialect.
