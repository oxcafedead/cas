package org.apereo.cas.clouddirectory;

import com.amazonaws.services.clouddirectory.AmazonCloudDirectory;
import com.amazonaws.services.clouddirectory.model.IndexAttachment;
import com.amazonaws.services.clouddirectory.model.ListIndexRequest;
import com.amazonaws.services.clouddirectory.model.ListIndexResult;
import com.amazonaws.services.clouddirectory.model.ListObjectAttributesRequest;
import com.amazonaws.services.clouddirectory.model.ListObjectAttributesResult;
import com.amazonaws.services.clouddirectory.model.ObjectReference;
import com.amazonaws.services.clouddirectory.model.TypedAttributeValue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.configuration.model.support.clouddirectory.CloudDirectoryProperties;
import org.apereo.cas.util.DateTimeUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCloudDirectoryRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultCloudDirectoryRepository implements CloudDirectoryRepository {
    private final AmazonCloudDirectory amazonCloudDirectory;
    private final CloudDirectoryProperties cloudDirectoryProperties;

    @Override
    public Map<String, Object> getUser(final String username) {
        return getUserInfoFromIndexResult(getIndexResult(username));
    }

    private ListIndexResult getIndexResult(final String username) {
        final var reference = CloudDirectoryUtils.getObjectRefByPath(cloudDirectoryProperties.getUsernameIndexPath());
        final var listIndexRequest = CloudDirectoryUtils.getListIndexRequest(
                cloudDirectoryProperties.getUsernameAttributeName(),
                username, reference, cloudDirectoryProperties);
        return amazonCloudDirectory.listIndex(listIndexRequest);
    }

    private Map<String, Object> getUserInfoFromIndexResult(final ListIndexResult indexResult) {
        final var attachment = indexResult.getIndexAttachments().stream().findFirst().orElse(null);

        if (attachment != null) {
            final var identifier = attachment.getObjectIdentifier();
            final var listObjectAttributesRequest =
                    CloudDirectoryUtils.getListObjectAttributesRequest(cloudDirectoryProperties.getDirectoryArn(), identifier);
            final var attributesResult = amazonCloudDirectory.listObjectAttributes(
                    listObjectAttributesRequest);

            if (attributesResult != null && attributesResult.getAttributes() != null) {
                return attributesResult.getAttributes()
                        .stream()
                        .map(a -> {
                            Object value = null;

                            final var attributeValue = a.getValue();
                            LOGGER.debug("Examining attribute [{}]", a);
                            
                            if (StringUtils.isNotBlank(attributeValue.getNumberValue())) {
                                value = attributeValue.getNumberValue();
                            } else if (attributeValue.getDatetimeValue() != null) {
                                value = DateTimeUtils.zonedDateTimeOf(attributeValue.getDatetimeValue()).toString();
                            } else if (attributeValue.getBooleanValue() != null) {
                                value = attributeValue.getBooleanValue().toString();
                            } else if (attributeValue.getBinaryValue() != null) {
                                value = new String(attributeValue.getBinaryValue().array(), StandardCharsets.UTF_8);
                            } else if (StringUtils.isNotBlank(attributeValue.getStringValue())) {
                                value = attributeValue.getStringValue();
                            }

                            return Pair.of(a.getKey().getName(), value);
                        })
                        .filter(p -> p.getValue() != null)
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            }
        }
        return null;
    }

}
