package com.lognex.api.entities.documents;

import com.lognex.api.clients.EntityApiClient;
import com.lognex.api.entities.MetaEntity;
import com.lognex.api.entities.Store;
import com.lognex.api.entities.agents.Counterparty;
import com.lognex.api.entities.agents.Organization;
import com.lognex.api.responses.ListEntity;
import com.lognex.api.responses.metadata.MetadataAttributeSharedStatesResponse;
import com.lognex.api.utils.LognexApiException;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.lognex.api.utils.params.FilterParam.filterEq;
import static org.junit.Assert.*;

public class SalesReturnTest extends DocumentWithPositionsTestBase {
    @Test
    public void createTest() throws IOException, LognexApiException {
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setName("salesreturn_" + randomString(3) + "_" + new Date().getTime());
        salesReturn.setDescription(randomString());
        salesReturn.setVatEnabled(true);
        salesReturn.setVatIncluded(true);
        salesReturn.setMoment(LocalDateTime.now());
        Organization organization = simpleEntityManager.getOwnOrganization();
        salesReturn.setOrganization(organization);
        Counterparty agent = simpleEntityManager.createSimple(Counterparty.class);
        salesReturn.setAgent(agent);
        Store mainStore = simpleEntityManager.getMainStore();
        salesReturn.setStore(mainStore);

        Demand demand = new Demand();
        demand.setName("demand_" + randomString(3) + "_" + new Date().getTime());
        demand.setDescription(randomString());
        demand.setOrganization(organization);
        demand.setAgent(agent);
        demand.setStore(mainStore);
        
        api.entity().demand().create(demand);
        salesReturn.setDemand(demand);

        api.entity().salesreturn().create(salesReturn);

        ListEntity<SalesReturn> updatedEntitiesList = api.entity().salesreturn().get(filterEq("name", salesReturn.getName()));
        assertEquals(1, updatedEntitiesList.getRows().size());

        SalesReturn retrievedEntity = updatedEntitiesList.getRows().get(0);
        assertEquals(salesReturn.getName(), retrievedEntity.getName());
        assertEquals(salesReturn.getDescription(), retrievedEntity.getDescription());
        assertEquals(salesReturn.getVatEnabled(), retrievedEntity.getVatEnabled());
        assertEquals(salesReturn.getVatIncluded(), retrievedEntity.getVatIncluded());
        assertEquals(salesReturn.getMoment(), retrievedEntity.getMoment());
        assertEquals(salesReturn.getOrganization().getMeta().getHref(), retrievedEntity.getOrganization().getMeta().getHref());
        assertEquals(salesReturn.getAgent().getMeta().getHref(), retrievedEntity.getAgent().getMeta().getHref());
        assertEquals(salesReturn.getStore().getMeta().getHref(), retrievedEntity.getStore().getMeta().getHref());
        assertEquals(salesReturn.getDemand().getMeta().getHref(), retrievedEntity.getDemand().getMeta().getHref());
    }

    @Test
    public void metadataTest() throws IOException, LognexApiException {
        MetadataAttributeSharedStatesResponse response = api.entity().salesreturn().metadata().get();

        assertFalse(response.getCreateShared());
    }

    @Test
    public void newTest() throws IOException, LognexApiException {
        SalesReturn salesReturn = api.entity().salesreturn().templateDocument();
        LocalDateTime time = LocalDateTime.now();

        assertEquals("", salesReturn.getName());
        assertTrue(salesReturn.getVatEnabled());
        assertTrue(salesReturn.getVatIncluded());
        assertEquals(Long.valueOf(0), salesReturn.getSum());
        assertFalse(salesReturn.getShared());
        assertTrue(salesReturn.getApplicable());
        assertTrue(ChronoUnit.MILLIS.between(time, salesReturn.getMoment()) < 1000);

        assertEquals(salesReturn.getOrganization().getMeta().getHref(), simpleEntityManager.getOwnOrganization().getMeta().getHref());
        assertEquals(salesReturn.getStore().getMeta().getHref(), simpleEntityManager.getMainStore().getMeta().getHref());
    }

    @Test
    public void newByDemandTest() throws IOException, LognexApiException {
        Demand demand = simpleEntityManager.createSimple(Demand.class);

        SalesReturn salesReturn = api.entity().salesreturn().templateDocument("demand", demand);
        LocalDateTime time = LocalDateTime.now();

        assertEquals("", salesReturn.getName());
        assertEquals(demand.getVatEnabled(), salesReturn.getVatEnabled());
        assertEquals(demand.getVatIncluded(), salesReturn.getVatIncluded());
        assertEquals(demand.getPayedSum(), salesReturn.getPayedSum());
        assertEquals(demand.getSum(), salesReturn.getSum());
        assertFalse(salesReturn.getShared());
        assertTrue(salesReturn.getApplicable());
        assertTrue(ChronoUnit.MILLIS.between(time, salesReturn.getMoment()) < 1000);
        assertEquals(demand.getMeta().getHref(), salesReturn.getDemand().getMeta().getHref());
        assertEquals(demand.getAgent().getMeta().getHref(), salesReturn.getAgent().getMeta().getHref());
        assertEquals(demand.getStore().getMeta().getHref(), salesReturn.getStore().getMeta().getHref());
        assertEquals(demand.getOrganization().getMeta().getHref(), salesReturn.getOrganization().getMeta().getHref());
    }

    @Override
    protected void getAsserts(MetaEntity originalEntity, MetaEntity retrievedEntity) {
        SalesReturn originalSalesReturn = (SalesReturn) originalEntity;
        SalesReturn retrievedSalesReturn = (SalesReturn) retrievedEntity;

        assertEquals(originalSalesReturn.getName(), retrievedSalesReturn.getName());
        assertEquals(originalSalesReturn.getOrganization().getMeta().getHref(), retrievedSalesReturn.getOrganization().getMeta().getHref());
        assertEquals(originalSalesReturn.getAgent().getMeta().getHref(), retrievedSalesReturn.getAgent().getMeta().getHref());
        assertEquals(originalSalesReturn.getStore().getMeta().getHref(), retrievedSalesReturn.getStore().getMeta().getHref());
    }

    @Override
    protected void putAsserts(MetaEntity originalEntity, MetaEntity updatedEntity, Object changedField) {
        SalesReturn originalSalesReturn = (SalesReturn) originalEntity;
        SalesReturn updatedSalesReturn = (SalesReturn) updatedEntity;

        assertNotEquals(originalSalesReturn.getName(), updatedSalesReturn.getName());
        assertEquals(changedField, updatedSalesReturn.getName());
        assertEquals(originalSalesReturn.getOrganization().getMeta().getHref(), updatedSalesReturn.getOrganization().getMeta().getHref());
        assertEquals(originalSalesReturn.getAgent().getMeta().getHref(), updatedSalesReturn.getAgent().getMeta().getHref());
        assertEquals(originalSalesReturn.getStore().getMeta().getHref(), updatedSalesReturn.getStore().getMeta().getHref());
    }

    @Override
    protected EntityApiClient entityClient() {
        return api.entity().salesreturn();
    }

    @Override
    protected Class<? extends MetaEntity> entityClass() {
        return SalesReturn.class;
    }
}
