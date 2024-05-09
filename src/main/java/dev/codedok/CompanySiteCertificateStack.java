package dev.codedok;

import dev.codedok.util.Domain;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.constructs.Construct;

import java.util.Optional;
import java.util.logging.Logger;

@Getter
public class CompanySiteCertificateStack extends Stack {

    private final Logger LOGGER = Logger.getLogger(getClass().getName());

    ICertificate certificate;

    public CompanySiteCertificateStack(final Construct construct, final StackProps props, final String id, @Nullable Domain domain) {
        super(construct, id, props);

        if (domain != null) {
            certificate = Certificate.Builder.create(this, "CertificateCompanySubdomain")
                    .certificateName("Company Subdomain Certificate")
                    .domainName(domain.getFullQualifiedDomain())
                    .validation(CertificateValidation.fromDns())
                    .build();
        } else {
            LOGGER.severe("Skipped stack: " + id);
        }
    }
}
