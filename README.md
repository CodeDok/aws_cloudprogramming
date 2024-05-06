# Welcome to the Cloud Programming Exercise 1 Repository

This repository contains all the information to create and deploy the hello world application 
for the module Cloud-Programming at IUBH. 

The `cdk.json` file tells the CDK Toolkit how to execute your app.
It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven compatible Java IDE to build and run tests.

The application contains two stacks: CompanySiteCertificateStack and CompanySiteStack.

The CompanySiteCertificateStack is necessary to create the SSL certificate which is necessary for the
HTTPS connection for the CompanySiteStack.
The CompanySiteStack contains all the artifacts and services we need to deploy the website.

## Prerequisites

- An IAM AWS Account with all the necessary permissions
- [AWS CDK Getting started](https://docs.aws.amazon.com/cdk/v2/guide/getting_started.html)
  - Most important:
    - Install CDK and necessary dependencies like NodeJs
- Docker Engine

## Steps to deploy to AWS

### Login

```shell
# Login
aws sso login
```

```shell
# Test connection
aws sts get-caller-identity
```

### Add Region to profile

#### Config Location Windows

`C:\Users\<Account>\.aws\config`

#### Config Location Linux/MacOs

`~/.aws/config`

There you have to add the Key-Value Entry `region = <Your Region>` for example:

````
[profile ***]
sso_session=default
sso_account_id=***
region = eu-north-1
sso_role_name=***
````

---

### Certificate Stack
1. Bootstrap the us-east-1 environment: 
    ```shell
    cdk bootstrap aws://<IAM ACCOUNT-NUMBER>/us-east-1
    ```
---   
2. Create CloudFormation template and deploy to AWS: 
    ```shell
   cdk deploy -c subdomain=<> -c domain=<> CompanySiteCertificateStack
    ```
   - subdomain: Your owned subdomain (ie. www, cloudprogramming, ...)
   - domain: Your owned domain (ie. thisATest.de, domain.com, ...)
---
3. Manually go into AWS Certificate Manager or copy the dns record information 
   and validate via DNS at your domain provider (Create DNS Entry with provided name and value in cli or AWS Management Console)
---
4. The stack should shortly finish successfully 


### Company Site Stack
1. Bootstrap your environment for example eu-north-1:
    ```shell
    cdk bootstrap aws://<IAM ACCOUNT-NUMBER>/eu-north-1
    ```  
2. Create CloudFormation template and deploy to AWS: 
    ````shell
    cdk deploy -c subdomain=<> -c domain=<> CompanySiteStack
    
    # ie cdk deploy -c subdomain=cloudprogramming -c domain=codedok.dev CompanySiteStack
    # => https://cloudprogramming.codedok.dev
    ````
   - Same subdomain and domains as for the certificate stack
3. The stack should shortly finish successfully 
4. Manually set the nameserver at your dns registrar to the one you find in the newly created hosted zone
5. Now you can access the website via the provided subdomain and domain

## Useful commands

 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation