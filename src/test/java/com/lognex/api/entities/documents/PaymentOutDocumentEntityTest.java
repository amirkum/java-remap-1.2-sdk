package com.lognex.api.entities.documents;

import com.lognex.api.entities.*;
import com.lognex.api.entities.agents.CounterpartyEntity;
import com.lognex.api.entities.agents.OrganizationEntity;
import com.lognex.api.responses.ListEntity;
import com.lognex.api.responses.metadata.MetadataAttributeSharedStatesResponse;
import com.lognex.api.utils.LognexApiException;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

import static com.lognex.api.utils.params.FilterParam.filterEq;
import static com.lognex.api.utils.params.SearchParam.search;
import static org.junit.Assert.*;

public class PaymentOutDocumentEntityTest extends EntityTestBase {
    @Test
    public void createTest() throws IOException, LognexApiException {
        PaymentOutDocumentEntity e = new PaymentOutDocumentEntity();
        e.setName("paymentout_" + randomString(3) + "_" + new Date().getTime());
        e.setMoment(LocalDateTime.now());
        e.setSum(randomLong(10, 10000));

        ListEntity<OrganizationEntity> orgList = api.entity().organization().get();
        assertNotEquals(0, orgList.getRows().size());
        e.setOrganization(orgList.getRows().get(0));

        CounterpartyEntity agent = new CounterpartyEntity();
        agent.setName(randomString());
        api.entity().counterparty().post(agent);
        e.setAgent(agent);

        ExpenseItemEntity expenseItem = new ExpenseItemEntity();
        expenseItem.setName(randomString());
        api.entity().expenseitem().post(expenseItem);
        e.setExpenseItem(expenseItem);

        api.entity().paymentout().post(e);

        ListEntity<PaymentOutDocumentEntity> updatedEntitiesList = api.entity().paymentout().get(filterEq("name", e.getName()));
        assertEquals(1, updatedEntitiesList.getRows().size());

        PaymentOutDocumentEntity retrievedEntity = updatedEntitiesList.getRows().get(0);
        assertEquals(e.getName(), retrievedEntity.getName());
        assertEquals(e.getMoment(), retrievedEntity.getMoment());
        assertEquals(e.getSum(), retrievedEntity.getSum());
        assertEquals(e.getOrganization().getMeta().getHref(), retrievedEntity.getOrganization().getMeta().getHref());
        assertEquals(e.getAgent().getMeta().getHref(), retrievedEntity.getAgent().getMeta().getHref());
        assertEquals(e.getExpenseItem().getMeta().getHref(), retrievedEntity.getExpenseItem().getMeta().getHref());
    }

    @Test
    public void getTest() throws IOException, LognexApiException {
        PaymentOutDocumentEntity e = createSimpleDocumentPaymentOut();

        PaymentOutDocumentEntity retrievedEntity = api.entity().paymentout().get(e.getId());
        getAsserts(e, retrievedEntity);

        retrievedEntity = api.entity().paymentout().get(e);
        getAsserts(e, retrievedEntity);
    }

    @Test
    public void putTest() throws IOException, LognexApiException, InterruptedException {
        PaymentOutDocumentEntity e = createSimpleDocumentPaymentOut();

        PaymentOutDocumentEntity retrievedOriginalEntity = api.entity().paymentout().get(e.getId());
        String name = "paymentout_" + randomString(3) + "_" + new Date().getTime();
        e.setName(name);
        api.entity().paymentout().put(e.getId(), e);
        putAsserts(e, retrievedOriginalEntity, name);

        retrievedOriginalEntity.set(e);

        name = "paymentout_" + randomString(3) + "_" + new Date().getTime();
        e.setName(name);
        api.entity().paymentout().put(e);
        putAsserts(e, retrievedOriginalEntity, name);
    }

    @Test
    public void deleteTest() throws IOException, LognexApiException {
        PaymentOutDocumentEntity e = createSimpleDocumentPaymentOut();

        ListEntity<PaymentOutDocumentEntity> entitiesList = api.entity().paymentout().get(filterEq("name", e.getName()));
        assertEquals((Integer) 1, entitiesList.getMeta().getSize());

        api.entity().paymentout().delete(e.getId());

        entitiesList = api.entity().paymentout().get(filterEq("name", e.getName()));
        assertEquals((Integer) 0, entitiesList.getMeta().getSize());
    }

    @Test
    public void deleteByIdTest() throws IOException, LognexApiException {
        PaymentOutDocumentEntity e = createSimpleDocumentPaymentOut();

        ListEntity<PaymentOutDocumentEntity> entitiesList = api.entity().paymentout().get(filterEq("name", e.getName()));
        assertEquals((Integer) 1, entitiesList.getMeta().getSize());

        api.entity().paymentout().delete(e);

        entitiesList = api.entity().paymentout().get(filterEq("name", e.getName()));
        assertEquals((Integer) 0, entitiesList.getMeta().getSize());
    }

    @Test
    public void metadataTest() throws IOException, LognexApiException {
        MetadataAttributeSharedStatesResponse response = api.entity().paymentout().metadata().get();

        assertFalse(response.getCreateShared());
    }

    @Test
    public void newTest() throws IOException, LognexApiException {
        PaymentOutDocumentEntity e = api.entity().paymentout().newDocument();
        LocalDateTime time = LocalDateTime.now().withNano(0);

        assertEquals("", e.getName());
        assertEquals(Long.valueOf(0), e.getSum());
        assertFalse(e.getShared());
        assertTrue(e.getApplicable());
        assertTrue(ChronoUnit.MILLIS.between(time, e.getMoment()) < 1000);

        ListEntity<OrganizationEntity> orgList = api.entity().organization().get();
        Optional<OrganizationEntity> orgOptional = orgList.getRows().stream().
                min(Comparator.comparing(OrganizationEntity::getCreated));

        OrganizationEntity org = null;
        if (orgOptional.isPresent()) {
            org = orgOptional.get();
        } else {
            // Должно быть первое созданное юрлицо
            fail();
        }

        assertEquals(e.getOrganization().getMeta().getHref(), org.getMeta().getHref());

        ListEntity<GroupEntity> group = api.entity().group().get(search("Основной"));
        assertEquals(1, group.getRows().size());
        assertEquals(e.getGroup().getMeta().getHref(), group.getRows().get(0).getMeta().getHref());
    }

    @Test
    public void newByPurchaseOrdersTest() throws IOException, LognexApiException {
        PurchaseOrderDocumentEntity purchaseOrder = new PurchaseOrderDocumentEntity();
        purchaseOrder.setName("purchaseorder_" + randomString(3) + "_" + new Date().getTime());
        purchaseOrder.setDescription(randomString());
        purchaseOrder.setVatEnabled(true);
        purchaseOrder.setVatIncluded(true);

        ListEntity<OrganizationEntity> orgList = api.entity().organization().get();
        assertNotEquals(0, orgList.getRows().size());
        purchaseOrder.setOrganization(orgList.getRows().get(0));

        CounterpartyEntity agent = new CounterpartyEntity();
        agent.setName(randomString());
        api.entity().counterparty().post(agent);
        purchaseOrder.setAgent(agent);
        purchaseOrder.setMoment(LocalDateTime.now().withNano(0));

        api.entity().purchaseorder().post(purchaseOrder);

        PaymentOutDocumentEntity e = api.entity().paymentout().newDocument("operations", Collections.singletonList(purchaseOrder));
        LocalDateTime time = LocalDateTime.now().withNano(0);

        assertEquals("", e.getName());
        assertEquals(purchaseOrder.getSum(), e.getSum());
        assertFalse(e.getShared());
        assertTrue(e.getApplicable());
        assertTrue(ChronoUnit.MILLIS.between(time, e.getMoment()) < 1000);
        assertEquals(1, e.getOperations().size());
        assertEquals(purchaseOrder.getMeta().getHref(), e.getOperations().get(0).getMeta().getHref());
        assertEquals(purchaseOrder.getGroup().getMeta().getHref(), e.getGroup().getMeta().getHref());
        assertEquals(purchaseOrder.getAgent().getMeta().getHref(), e.getAgent().getMeta().getHref());
        assertEquals(purchaseOrder.getOrganization().getMeta().getHref(), e.getOrganization().getMeta().getHref());
    }

    @Test
    public void newBySalesReturnsTest() throws IOException, LognexApiException {
        SalesReturnDocumentEntity salesReturn = new SalesReturnDocumentEntity();
        salesReturn.setName("salesreturn_" + randomString(3) + "_" + new Date().getTime());

        ListEntity<OrganizationEntity> orgList = api.entity().organization().get();
        assertNotEquals(0, orgList.getRows().size());
        salesReturn.setOrganization(orgList.getRows().get(0));

        CounterpartyEntity agent = new CounterpartyEntity();
        agent.setName(randomString());
        api.entity().counterparty().post(agent);
        salesReturn.setAgent(agent);

        ListEntity<StoreEntity> store = api.entity().store().get(filterEq("name", "Основной склад"));
        assertEquals(1, store.getRows().size());
        salesReturn.setStore(store.getRows().get(0));

        DemandDocumentEntity demand = new DemandDocumentEntity();
        demand.setName("demand_" + randomString(3) + "_" + new Date().getTime());
        demand.setDescription(randomString());
        demand.setOrganization(orgList.getRows().get(0));
        demand.setAgent(agent);
        demand.setStore(store.getRows().get(0));

        api.entity().demand().post(demand);
        salesReturn.setDemand(demand);

        api.entity().salesreturn().post(salesReturn);

        PaymentOutDocumentEntity e = api.entity().paymentout().newDocument("operations", Collections.singletonList(salesReturn));
        LocalDateTime time = LocalDateTime.now().withNano(0);

        assertEquals("", e.getName());
        assertEquals(salesReturn.getSum(), e.getSum());
        assertFalse(e.getShared());
        assertTrue(e.getApplicable());
        assertTrue(ChronoUnit.MILLIS.between(time, e.getMoment()) < 1000);
        assertEquals(1, e.getOperations().size());
        assertEquals(salesReturn.getMeta().getHref(), e.getOperations().get(0).getMeta().getHref());
        assertEquals(salesReturn.getGroup().getMeta().getHref(), e.getGroup().getMeta().getHref());
        assertEquals(salesReturn.getAgent().getMeta().getHref(), e.getAgent().getMeta().getHref());
        assertEquals(salesReturn.getOrganization().getMeta().getHref(), e.getOrganization().getMeta().getHref());
    }

    @Test
    public void newBySuppliesTest() throws IOException, LognexApiException {
        SupplyDocumentEntity supply = new SupplyDocumentEntity();
        supply.setName("supply_" + randomString(3) + "_" + new Date().getTime());

        ListEntity<OrganizationEntity> orgList = api.entity().organization().get();
        assertNotEquals(0, orgList.getRows().size());
        supply.setOrganization(orgList.getRows().get(0));

        CounterpartyEntity agent = new CounterpartyEntity();
        agent.setName(randomString());
        api.entity().counterparty().post(agent);
        supply.setAgent(agent);

        ListEntity<StoreEntity> store = api.entity().store().get(filterEq("name", "Основной склад"));
        assertEquals(1, store.getRows().size());
        supply.setStore(store.getRows().get(0));

        api.entity().supply().post(supply);

        PaymentOutDocumentEntity e = api.entity().paymentout().newDocument("operations", Collections.singletonList(supply));
        LocalDateTime time = LocalDateTime.now().withNano(0);

        assertEquals("", e.getName());
        assertEquals(supply.getSum(), e.getSum());
        assertFalse(e.getShared());
        assertTrue(e.getApplicable());
        assertTrue(ChronoUnit.MILLIS.between(time, e.getMoment()) < 1000);
        assertEquals(1, e.getOperations().size());
        assertEquals(supply.getMeta().getHref(), e.getOperations().get(0).getMeta().getHref());
        assertEquals(supply.getGroup().getMeta().getHref(), e.getGroup().getMeta().getHref());
        assertEquals(supply.getAgent().getMeta().getHref(), e.getAgent().getMeta().getHref());
        assertEquals(supply.getOrganization().getMeta().getHref(), e.getOrganization().getMeta().getHref());
    }

    @Test
    public void newByInvoicesInTest() throws IOException, LognexApiException {
        InvoiceInDocumentEntity invoiceIn = new InvoiceInDocumentEntity();
        invoiceIn.setName("invoiceout_" + randomString(3) + "_" + new Date().getTime());
        invoiceIn.setVatEnabled(true);
        invoiceIn.setVatIncluded(true);

        ListEntity<OrganizationEntity> orgList = api.entity().organization().get();
        assertNotEquals(0, orgList.getRows().size());
        invoiceIn.setOrganization(orgList.getRows().get(0));

        CounterpartyEntity agent = new CounterpartyEntity();
        agent.setName(randomString());
        api.entity().counterparty().post(agent);
        invoiceIn.setAgent(agent);

        api.entity().invoicein().post(invoiceIn);

        PaymentOutDocumentEntity e = api.entity().paymentout().newDocument("operations", Collections.singletonList(invoiceIn));
        LocalDateTime time = LocalDateTime.now().withNano(0);

        assertEquals("", e.getName());
        assertEquals(invoiceIn.getSum(), e.getSum());
        assertFalse(e.getShared());
        assertTrue(e.getApplicable());
        assertTrue(ChronoUnit.MILLIS.between(time, e.getMoment()) < 1000);
        assertEquals(1, e.getOperations().size());
        assertEquals(invoiceIn.getMeta().getHref(), e.getOperations().get(0).getMeta().getHref());
        assertEquals(invoiceIn.getGroup().getMeta().getHref(), e.getGroup().getMeta().getHref());
        assertEquals(invoiceIn.getAgent().getMeta().getHref(), e.getAgent().getMeta().getHref());
        assertEquals(invoiceIn.getOrganization().getMeta().getHref(), e.getOrganization().getMeta().getHref());
    }

    @Test
    public void newByCommissionReportsOutTest() throws IOException, LognexApiException {
        CommissionReportOutDocumentEntity commissionReportOut = new CommissionReportOutDocumentEntity();
        commissionReportOut.setName("commissionreportout_" + randomString(3) + "_" + new Date().getTime());

        ListEntity<OrganizationEntity> orgList = api.entity().organization().get();
        assertNotEquals(0, orgList.getRows().size());
        commissionReportOut.setOrganization(orgList.getRows().get(0));

        CounterpartyEntity agent = new CounterpartyEntity();
        agent.setName(randomString());
        api.entity().counterparty().post(agent);
        commissionReportOut.setAgent(agent);

        ContractEntity contract = new ContractEntity();
        contract.setName(randomString());
        contract.setOwnAgent(orgList.getRows().get(0));
        contract.setAgent(agent);
        contract.setContractType(ContractEntity.Type.commission);
        api.entity().contract().post(contract);
        commissionReportOut.setContract(contract);

        commissionReportOut.setCommissionPeriodStart(LocalDateTime.now());
        commissionReportOut.setCommissionPeriodEnd(LocalDateTime.now().plusNanos(50));

        api.entity().commissionreportout().post(commissionReportOut);

        PaymentOutDocumentEntity e = api.entity().paymentout().newDocument("operations", Collections.singletonList(commissionReportOut));
        LocalDateTime time = LocalDateTime.now().withNano(0);

        assertEquals("", e.getName());
        assertEquals(commissionReportOut.getSum(), e.getSum());
        assertFalse(e.getShared());
        assertTrue(e.getApplicable());
        assertTrue(ChronoUnit.MILLIS.between(time, e.getMoment()) < 1000);
        assertEquals(1, e.getOperations().size());
        assertEquals(commissionReportOut.getMeta().getHref(), e.getOperations().get(0).getMeta().getHref());
        assertEquals(commissionReportOut.getGroup().getMeta().getHref(), e.getGroup().getMeta().getHref());
        assertEquals(commissionReportOut.getContract().getMeta().getHref(), e.getContract().getMeta().getHref());
        assertEquals(commissionReportOut.getAgent().getMeta().getHref(), e.getAgent().getMeta().getHref());
        assertEquals(commissionReportOut.getOrganization().getMeta().getHref(), e.getOrganization().getMeta().getHref());
    }

    private PaymentOutDocumentEntity createSimpleDocumentPaymentOut() throws IOException, LognexApiException {
        PaymentOutDocumentEntity e = new PaymentOutDocumentEntity();
        e.setName("paymentout_" + randomString(3) + "_" + new Date().getTime());

        ListEntity<OrganizationEntity> orgList = api.entity().organization().get();
        assertNotEquals(0, orgList.getRows().size());
        e.setOrganization(orgList.getRows().get(0));

        CounterpartyEntity agent = new CounterpartyEntity();
        agent.setName(randomString());
        api.entity().counterparty().post(agent);
        e.setAgent(agent);

        ExpenseItemEntity expenseItem = new ExpenseItemEntity();
        expenseItem.setName(randomString());
        api.entity().expenseitem().post(expenseItem);
        e.setExpenseItem(expenseItem);

        api.entity().paymentout().post(e);

        return e;
    }

    private void getAsserts(PaymentOutDocumentEntity e, PaymentOutDocumentEntity retrievedEntity) {
        assertEquals(e.getName(), retrievedEntity.getName());
        assertEquals(e.getOrganization().getMeta().getHref(), retrievedEntity.getOrganization().getMeta().getHref());
        assertEquals(e.getAgent().getMeta().getHref(), retrievedEntity.getAgent().getMeta().getHref());
        assertEquals(e.getExpenseItem().getMeta().getHref(), retrievedEntity.getExpenseItem().getMeta().getHref());
    }

    private void putAsserts(PaymentOutDocumentEntity e, PaymentOutDocumentEntity retrievedOriginalEntity, String name) throws IOException, LognexApiException {
        PaymentOutDocumentEntity retrievedUpdatedEntity = api.entity().paymentout().get(e.getId());

        assertNotEquals(retrievedOriginalEntity.getName(), retrievedUpdatedEntity.getName());
        assertEquals(name, retrievedUpdatedEntity.getName());
        assertEquals(retrievedOriginalEntity.getOrganization().getMeta().getHref(), retrievedUpdatedEntity.getOrganization().getMeta().getHref());
        assertEquals(retrievedOriginalEntity.getAgent().getMeta().getHref(), retrievedUpdatedEntity.getAgent().getMeta().getHref());
        assertEquals(retrievedOriginalEntity.getExpenseItem().getMeta().getHref(), retrievedUpdatedEntity.getExpenseItem().getMeta().getHref());
    }
}
