# spark-java-crawler-api
Simple Rest API to crawl websites set on env variable.

Set your environment variable BASE_URL to your desired url:
`export BASE_URL=http://www.my.website.com/`

Run the API's jar

Or with Docker:

On project's root
`docker build . -t spark-java/crawling-api`

`docker run
-e BASE_URL=http://www.my.website.com/
-p 4567:4567 --rm spark-java/crawling-api`
