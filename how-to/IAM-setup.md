# Setup IAM account and permissions

To be able to execute the CDK you will need to setup you AWS IAM infrastructure.

## Create policy in IAM
Create the policy for the IAM account. It will provide the necessary permissions for the Cloud 
Development Kit.


1. In the AWS Console search for `IAM`
2. Navigate to `Policies`
3. Click on `Create policy` on the top right corner
4. Choose `JSON`
5. Paste the content of the follow file into the Editor: [cdkPolicy.json](..%2Fpolicies%2FcdkPolicy.json)
6. Continue
7. Give it a name like `CloudDevelopmentKit`
8. Finally press on `Create policy`


## Activate IAM Identity Center in AWS and create iam account 
We will use the IAM Identity Center to create a user and a group with certain permissions matched to use 
CDK as the IAM instead of the core user.

1. In the AWS Console search for `IAM Identity Center`
2. Click on `Activate IAM Identity Center` in Organisation Mode
3. Move to `Users` in the left navigation bar
4. Click on `Add user` in the top right corner
5. Fill out the fields and finish creating the user
6. Navigate to the `Groups` field on the left navigation bar
7. Press on `Create group`
8. Give it a name and add the created user


## Create permission set for our infrastructure 

1. Move to `Permission sets` in the left navigation bar
2. Click on `Create permission set`
3. Choose `Custom permission set`
4. Under `Customer managed policies` add the policy you created before
5. Under `Inline policy` add the policy from [inline-policies.json](..%2Fpolicies%2Finline-policies.json) 
    - Note: Replace the placeholders with the name you will use for the permission set
6. Give it a name like `CloudDeveloper`
7. Finish the creation





