package dev.codedok;

import dev.codedok.util.Domain;
import lombok.val;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.net.URISyntaxException;

public class CloudProgrammingApp {

    public static void main(final String[] args) throws URISyntaxException {
        App app = new App();


        Domain domain = new Domain("cloudprogramming", "codedok.dev");

        Environment coreEnvironment = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region("eu-north-1")
                .build();

        Environment cloudFrontEnvironment = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region("us-east-1")
                .build();

        StackProps props = StackProps.builder()
                .env(coreEnvironment)
                .crossRegionReferences(true)
                .build();

        StackProps certProps = StackProps.builder()
                .env(cloudFrontEnvironment)
                .crossRegionReferences(true)
                .build();

        val companySiteCertificateStack = new CompanySiteCertificateStack(app, certProps, "CompanySiteCertificateStack", domain);
        val companySiteCertificate = companySiteCertificateStack.getCertificate();

        val companySiteStack = new CoreCompanySiteStack(app, "CompanySiteStack", props, companySiteCertificate, domain);

        companySiteStack.addDependency(companySiteCertificateStack, "We need the certificate from ACM");
        app.synth();
    }

}

