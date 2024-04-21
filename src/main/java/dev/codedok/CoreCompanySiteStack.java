package dev.codedok;

import dev.codedok.util.Domain;
import lombok.val;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.amazon.awscdk.services.cloudfront.BehaviorOptions;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.OriginProtocolPolicy;
import software.amazon.awscdk.services.cloudfront.ViewerProtocolPolicy;
import software.amazon.awscdk.services.cloudfront.origins.LoadBalancerV2Origin;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecr.assets.DockerImageAsset;
import software.amazon.awscdk.services.ecr.assets.Platform;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateServiceProps;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationProtocol;
import software.amazon.awscdk.services.route53.AaaaRecord;
import software.amazon.awscdk.services.route53.PublicHostedZone;
import software.amazon.awscdk.services.route53.RecordTarget;
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget;
import software.constructs.Construct;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class CoreCompanySiteStack extends Stack {

    ApplicationLoadBalancer applicationLoadBalancer;

    public CoreCompanySiteStack(final Construct scope, final String id, final StackProps props, ICertificate certificate, Domain domain) throws URISyntaxException{
        super(scope, id, props);


        VpcProps vpcProps = VpcProps.builder()
                .vpcName("CompanySiteVPC")
                .maxAzs(2)
                .build();
        Vpc vpc = new Vpc(this, "CompanySiteVPC", vpcProps);

        val securityGroup = SecurityGroup.Builder.create(this, "CompanySiteSecurityGroup")
                .securityGroupName("CompanySiteSecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .disableInlineRules(true)
                .build();

        securityGroup.addIngressRule(
                // Peer.prefixList("com.amazonaws.global.cloudfront.origin-facing"),
                Peer.prefixList("pl-fab65393"),
                Port.tcp(80),
                "Allow HTTP Access for CloudFront"
        );

        createEcsServiceForCompanySite(vpc, securityGroup);

        val cloudfrontDistributions = Distribution.Builder.create(this, "cloudFront")
                .enabled(true)
                .certificate(certificate)
                .defaultBehavior(BehaviorOptions.builder()
                        .origin(LoadBalancerV2Origin.Builder.create(applicationLoadBalancer)
                                .originShieldEnabled(false)
                                .httpsPort(80)
                                .protocolPolicy(OriginProtocolPolicy.HTTP_ONLY)
                                .build())
                        .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                        .build())
                .domainNames(List.of(domain.getFullQualifiedDomain()))
                .build();


        val publicDomainZone = PublicHostedZone.Builder.create(this, "codedok.dev")
                .zoneName(domain.getHost())
                .build();

        AaaaRecord.Builder.create(this, "Alias to CloudFront")
                .zone(publicDomainZone)
                .recordName(domain.getSubdomain())
                .target(RecordTarget.fromAlias(new CloudFrontTarget(cloudfrontDistributions)))
                .build();
    }



    private ApplicationLoadBalancedFargateService createEcsServiceForCompanySite(Vpc vpc, SecurityGroup securityGroup) throws URISyntaxException {
        URI dockerFolderURI = Objects.requireNonNull(getClass().getClassLoader().getResource("docker")).toURI();
        File dockerFolder = new File(dockerFolderURI);

        FargateTaskDefinition companyImageTaskDefinition = FargateTaskDefinition.Builder.create(this, "CompanyImageTaskDefintion")
                        .cpu(256)
                        .memoryLimitMiB(1024)
                        .build();

        val companyImageAsset = DockerImageAsset.Builder.create(this, "CompanySiteImage")
                .assetName("CompanySiteImage")
                .directory(new File(dockerFolder, "company_site_image").toString())
                .platform(Platform.LINUX_AMD64)
                .build();

        val companySiteContainer = companyImageTaskDefinition.addContainer("CompanyImageContainer",
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromDockerImageAsset(companyImageAsset))
                        .build()
        );

        companySiteContainer.addPortMappings(
                PortMapping.builder()
                        .hostPort(80)
                        .containerPort(80)
                        .protocol(Protocol.TCP)
                        .build()
        );

        val cluster = new Cluster(this, "CompanySiteCluster",
                ClusterProps.builder()
                        .vpc(vpc)
                        .build()
        );

        applicationLoadBalancer = ApplicationLoadBalancer.Builder.create(this, "LoadBalancer")
                .vpc(vpc)
                .internetFacing(true) // For CloudFront. Restricted via Prefixes from SecurityGroup
                .loadBalancerName("CompanySiteLoadBalancer")
                .securityGroup(securityGroup)
                .build();


        val albFargateService = new ApplicationLoadBalancedFargateService(this, "CompanySiteService",
                ApplicationLoadBalancedFargateServiceProps.builder()
                        .cluster(cluster)
                        .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromDockerImageAsset(companyImageAsset))
                                .build())
                        .protocol(ApplicationProtocol.HTTP)
                        .openListener(false)
                        .assignPublicIp(false)
                        .loadBalancer(applicationLoadBalancer)
                        .securityGroups(List.of(securityGroup))
                        .build()
        );

        albFargateService.getService()
                .getConnections()
                .getSecurityGroups()
                .get(0)
                .addIngressRule(Peer.ipv4(vpc.getVpcCidrBlock()), Port.tcp(80), "allow http inbound from vpc");

        albFargateService.getService()
                .autoScaleTaskCount(EnableScalingProps.builder()
                        .minCapacity(2)
                        .maxCapacity(4)
                        .build());

        // No need for spreading across the availability zones because fargate automatically does it.

        return albFargateService;
    }

}
