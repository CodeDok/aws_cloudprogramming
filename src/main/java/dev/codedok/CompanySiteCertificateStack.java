package dev.codedok;

import dev.codedok.util.Domain;
import lombok.Getter;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.constructs.Construct;

@Getter
public class CompanySiteCertificateStack extends Stack {

    ICertificate certificate;

    public CompanySiteCertificateStack(final Construct construct, final StackProps props, final String id, Domain domain) {
        super(construct, id, props);

        certificate = Certificate.Builder.create(this, "CertificateCompanySubdomain")
                .certificateName("Company Subdomain Certificate")
                .domainName(domain.getFullQualifiedDomain())
                .validation(CertificateValidation.fromDns())
                .build();
    }



}
