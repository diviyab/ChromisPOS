//    Chromis POS  - The New Face of Open Source POS
//    Copyright (c) 2015 
//    http://www.chromis.co.uk liquibase
//
//    This file is part of Chromis POS
//
//     Chromis POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Chromis POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>.
package uk.chromis.pos.forms;

import uk.chromis.basic.BasicException;
import uk.chromis.data.loader.*;
import uk.chromis.data.model.Field;
import uk.chromis.data.model.Row;
import uk.chromis.format.Formats;
import uk.chromis.pos.customers.CustomerInfoExt;
import uk.chromis.pos.customers.CustomerTransaction;
import uk.chromis.pos.inventory.*;
import uk.chromis.pos.mant.FloorsInfo;
import uk.chromis.pos.payment.PaymentInfo;
import uk.chromis.pos.payment.PaymentInfoTicket;
import uk.chromis.pos.promotion.PromoInfo;
import uk.chromis.pos.promotion.PromoTypeInfo;
import uk.chromis.pos.ticket.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author adrianromero
 */
public class DataLogicSales extends BeanFactoryDataSingle {

    protected Session s;
    protected Datas[] auxiliarDatas;
    protected Datas[] stockdiaryDatas;
    protected SentenceExec m_sellvoucher;
    protected Datas[] paymenttabledatas;
    protected Datas[] stockdatas;
    protected Row productsRow;
    private String pName;
    private Double getTotal;
    private Double getTendered;
    private String getRetMsg;
    public static final String DEBT = "debt";
    public static final String DEBT_PAID = "debtpaid";
    protected static final String PREPAY = "prepay";
    private static final Logger logger = Logger.getLogger("uk.chromis.pos.forms.DataLogicSales");
    private String getCardName;

    private SentenceExec m_updateRefund;

    /**
     * Creates a new instance of SentenceContainerGeneric
     */
    public DataLogicSales() {
        stockdiaryDatas = new Datas[]{
            Datas.STRING,
            Datas.TIMESTAMP,
            Datas.INT,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.STRING};
        paymenttabledatas = new Datas[]{
            Datas.STRING,
            Datas.STRING,
            Datas.TIMESTAMP,
            Datas.STRING,
            Datas.STRING,
            Datas.DOUBLE,
            Datas.STRING};
        stockdatas = new Datas[]{
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.DOUBLE,
            Datas.DOUBLE,
            Datas.DOUBLE};
        auxiliarDatas = new Datas[]{
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.STRING};

        productsRow = new Row(
                new Field("ID", Datas.STRING, Formats.STRING), //0
                new Field(AppLocal.getIntString("label.prodref"), Datas.STRING, Formats.STRING, true, true, true), //1
                new Field(AppLocal.getIntString("label.prodbarcode"), Datas.STRING, Formats.STRING, false, true, true), //2
                new Field(AppLocal.getIntString("label.prodbarcodetype"), Datas.STRING, Formats.STRING, false, true, true), //3
                new Field(AppLocal.getIntString("label.prodname"), Datas.STRING, Formats.STRING, true, true, true), //4
                new Field("ISCOM", Datas.BOOLEAN, Formats.BOOLEAN), //5
                new Field("ISSCALE", Datas.BOOLEAN, Formats.BOOLEAN), //6
                new Field(AppLocal.getIntString("label.prodpricebuy"), Datas.DOUBLE, Formats.CURRENCY, false, true, true), //7
                new Field(AppLocal.getIntString("label.prodpricesell"), Datas.DOUBLE, Formats.CURRENCY, false, true, true), //8
                new Field(AppLocal.getIntString("label.prodcategory"), Datas.STRING, Formats.STRING, false, false, true), //9
                new Field(AppLocal.getIntString("label.taxcategory"), Datas.STRING, Formats.STRING, false, false, true), //10
                new Field(AppLocal.getIntString("label.attributeset"), Datas.STRING, Formats.STRING, false, false, true), //11
                new Field("IMAGE", Datas.IMAGE, Formats.NULL), //12
                new Field("STOCKCOST", Datas.DOUBLE, Formats.CURRENCY), //13
                new Field("STOCKVOLUME", Datas.DOUBLE, Formats.DOUBLE), //14
                new Field("ISCATALOG", Datas.BOOLEAN, Formats.BOOLEAN), //15
                new Field("CATORDER", Datas.INT, Formats.INT), //16
                new Field("PROPERTIES", Datas.BYTES, Formats.NULL), //17
                new Field("ISKITCHEN", Datas.BOOLEAN, Formats.BOOLEAN), //18
                new Field("ISSERVICE", Datas.BOOLEAN, Formats.BOOLEAN), //19
                new Field(AppLocal.getIntString("label.display"), Datas.STRING, Formats.STRING, false, true, true), //20
                new Field("ISVPRICE", Datas.BOOLEAN, Formats.BOOLEAN), //21
                new Field("ISVERPATRIB", Datas.BOOLEAN, Formats.BOOLEAN), //22
                new Field("TEXTTIP", Datas.STRING, Formats.STRING), //23
                new Field("WARRANTY", Datas.BOOLEAN, Formats.BOOLEAN), //24
                new Field(AppLocal.getIntString("label.stockunits"), Datas.DOUBLE, Formats.DOUBLE), //25     
                new Field("ALIAS", Datas.STRING, Formats.STRING), //26
                new Field("ALWAYSAVAILABLE", Datas.BOOLEAN, Formats.BOOLEAN), //27
                new Field("DISCOUNTED", Datas.STRING, Formats.STRING), //28
                new Field("CANDISCOUNT", Datas.BOOLEAN, Formats.BOOLEAN), //29
                new Field("ISPACK", Datas.BOOLEAN, Formats.BOOLEAN), //30
                new Field("PACKQUANTITY", Datas.DOUBLE, Formats.DOUBLE), //31
                new Field("PACKPRODUCT", Datas.STRING, Formats.STRING) //32
        );
    }

    /**
     *
     * @param s
     */
    @Override
    public void init(Session s) {
        this.s = s;

        m_updateRefund = new StaticSentence(s, "UPDATE TICKETLINES SET REFUNDQTY = ? WHERE TICKET = ? AND LINE = ?  ", new SerializerWriteBasic(new Datas[]{
            Datas.DOUBLE,
            Datas.STRING,
            Datas.INT
        }));

        m_sellvoucher = new StaticSentence(s, "INSERT INTO VOUCHERS ( VOUCHER, SOLDTICKETID) "
                + "VALUES (?, ?)", new SerializerWriteBasic(new Datas[]{
            Datas.STRING,
            Datas.STRING}));
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final Object getCategoryColour(String id) throws BasicException {

        return new PreparedSentence(s,
                "SELECT COLOUR FROM CATEGORIES WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE).find(id);
    }

    /**
     *
     * @return
     */
    public final Row getProductsRow() {
        return productsRow;
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final ProductInfoExt getProductInfo(String id) throws BasicException {
        return (ProductInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, " //1,2,3,4
                + "ISCOM, ISSCALE, " //5,6
                + "PRICEBUY, PRICESELL, " //7,8
                + "TAXCAT, CATEGORY, " //9,10
                + "ATTRIBUTESET_ID, " //11
                + "IMAGE, " //12
                + "ATTRIBUTES, " //13
                + "ISKITCHEN, ISSERVICE, " //14,15
                + "DISPLAY, " //16
                + "ISVPRICE, ISVERPATRIB, " //17,18
                + "TEXTTIP, WARRANTY, " //19,20
                + "STOCKCURRENT.UNITS, " //21  
                + "ALIAS, " //22
                + "ALWAYSAVAILABLE, " //23  
                + "DISCOUNTED, CANDISCOUNT, ISPACK, PACKQUANTITY, PACKPRODUCT "
                + "FROM STOCKCURRENT LEFT JOIN PRODUCTS ON (STOCKCURRENT.PRODUCT = PRODUCTS.ID) "
                //   + "WHERE ID = ? "
                //   + "GROUP BY ID, REFERENCE, NAME;", SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead()).find(id);
                + "WHERE ID = ?", SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead()).find(id);
    }

    /**
     *
     * @param sCode
     * @return
     * @throws BasicException
     */
    public final ProductInfoExt getProductInfoByCode(String sCode) throws BasicException {
        return (ProductInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, " //1,2,3,4
                + "ISCOM, ISSCALE, " //5,6
                + "PRICEBUY, PRICESELL, " //7,8
                + "TAXCAT, CATEGORY, " //9,10
                + "ATTRIBUTESET_ID, " //11
                + "IMAGE, " //12
                + "ATTRIBUTES, " //13
                + "ISKITCHEN, ISSERVICE, " //14,15
                + "DISPLAY, " //16
                + "ISVPRICE, ISVERPATRIB, " //17,18
                + "TEXTTIP, WARRANTY, " //19,20
                + "STOCKCURRENT.UNITS, " //21   
                + "ALIAS, " //22
                + "ALWAYSAVAILABLE, " //23   
                + "DISCOUNTED, CANDISCOUNT, ISPACK, PACKQUANTITY, PACKPRODUCT "
                //                + "FROM STOCKCURRENT LEFT JOIN PRODUCTS ON (STOCKCURRENT.PRODUCT = PRODUCTS.ID) "
                + "FROM STOCKCURRENT RIGHT JOIN PRODUCTS ON (STOCKCURRENT.PRODUCT = PRODUCTS.ID) "
                + "WHERE CODE = ?", SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead()).find(sCode);
    }

    /**
     *
     * @param sReference
     * @return
     * @throws BasicException
     */
    public final ProductInfoExt getProductInfoByReference(String sReference) throws BasicException {
        return (ProductInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, " //1,2,3,4
                + "ISCOM, ISSCALE, " //5,6
                + "PRICEBUY, PRICESELL, " //7,8
                + "TAXCAT, CATEGORY, " //9,10
                + "ATTRIBUTESET_ID, " //11
                + "IMAGE, " //12
                + "ATTRIBUTES, " //13
                + "ISKITCHEN, ISSERVICE, " //14,15
                + "DISPLAY, " //16
                + "ISVPRICE, ISVERPATRIB, " //17,18
                + "TEXTTIP, WARRANTY, " //19,20
                + "STOCKCURRENT.UNITS, " //21 
                + "ALIAS, " //22
                + "ALWAYSAVAILABLE, " //23
                + "DISCOUNTED, CANDISCOUNT, ISPACK, PACKQUANTITY, PACKPRODUCT "
                + "FROM STOCKCURRENT RIGHT JOIN PRODUCTS ON (STOCKCURRENT.PRODUCT = PRODUCTS.ID) "
                + "WHERE REFERENCE = ?", SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead()).find(sReference);
    }

    public final ProductInfoExt getProductInfoNoSC(String id) throws BasicException {
        return (ProductInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, " //1,2,3,4
                + "ISCOM, ISSCALE, " //5,6
                + "PRICEBUY, PRICESELL, " //7,8
                + "TAXCAT, CATEGORY, " //9,10
                + "ATTRIBUTESET_ID, " //11
                + "IMAGE, " //12
                + "ATTRIBUTES, " //13
                + "ISKITCHEN, ISSERVICE, " //14,15
                + "DISPLAY, " //16
                + "ISVPRICE, ISVERPATRIB, " //17,18
                + "TEXTTIP, WARRANTY, " //19,20
                + "STOCKUNITS, " //21
                + "ALIAS, " //22
                + "ALWAYSAVAILABLE, " //23   
                + "DISCOUNTED, CANDISCOUNT, ISPACK, PACKQUANTITY, PACKPRODUCT "
                + "FROM PRODUCTS WHERE ID = ? "
                //+ "GROUP BY ID, REFERENCE, NAME "
                // JL changed GROUP BY to ORDER BY, as it does not work with embedded for multiple grouping , 
                // can find no reason for the group by use at the moment        
                + "ORDER BY ID, REFERENCE, NAME ", SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead()).find(id);
    }

    /**
     *
     * @return @throws BasicException
     */
    public final List<CategoryInfo> getRootCategories() throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "COLOUR, "
                + "CATORDER "
                + "FROM CATEGORIES "
                + "WHERE PARENTID IS NULL AND CATSHOWNAME = " + s.DB.TRUE() + " "
                + "ORDER BY NAME", null, CategoryInfo.getSerializerRead()).list();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final List<CategoryInfo> getRootCategoriesByCatOrder() throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "COLOUR, "
                + "CATORDER "
                + "FROM CATEGORIES "
                + "WHERE PARENTID IS NULL AND CATSHOWNAME = " + s.DB.TRUE() + " AND CATORDER IS NOT NULL "
                + "ORDER BY CATORDER", null, CategoryInfo.getSerializerRead()).list();
                   }

    public final List<CategoryInfo> getRootCategoriesByName() throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "COLOUR, "
                + "CATORDER "
                + "FROM CATEGORIES "
                + "WHERE PARENTID IS NULL AND CATSHOWNAME = " + s.DB.TRUE() + " AND CATORDER IS NULL "
                + "ORDER BY NAME", null, CategoryInfo.getSerializerRead()).list();
                   }    
    
    
    /**
     *
     * @param category
     * @return
     * @throws BasicException
     */
    public final List<CategoryInfo> getSubcategories(String category) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "COLOUR, "
                + "CATORDER "
                + "FROM CATEGORIES WHERE PARENTID = ? ORDER BY NAME", SerializerWriteString.INSTANCE, CategoryInfo.getSerializerRead()).list(category);
    }

    public final List<CategoryInfo> getSubcategoriesByCatOrder(String category) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "COLOUR, "
                + "CATORDER "
                + "FROM CATEGORIES WHERE PARENTID = ? AND CATORDER IS NOT NULL ORDER BY CATORDER", SerializerWriteString.INSTANCE, CategoryInfo.getSerializerRead()).list(category);                
    }
    
    public final List<CategoryInfo> getSubcategoriesByName(String category) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "COLOUR, "
                + "CATORDER "
                + "FROM CATEGORIES WHERE PARENTID = ? AND CATORDER IS NULL ORDER BY NAME", SerializerWriteString.INSTANCE, CategoryInfo.getSerializerRead()).list(category);                
    }
    
    
    /**
     *
     * @param category
     * @return
     * @throws BasicException
     */
    public List<ProductInfoExt> getProductCatalog(String category) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.CODETYPE, "
                + "P.NAME, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.TAXCAT, "
                + "P.CATEGORY, "
                + "P.ATTRIBUTESET_ID, "
                + "P.IMAGE, "
                + "P.ATTRIBUTES, "
                + "P.ISKITCHEN, "
                + "P.ISSERVICE, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.ALIAS, "
                + "P.ALWAYSAVAILABLE, "
                + "P.DISCOUNTED, "
                + "P.CANDISCOUNT, "
                + "P.ISPACK, P.PACKQUANTITY, P.PACKPRODUCT "
                + "FROM PRODUCTS P, PRODUCTS_CAT O "
                + "WHERE P.ID = O.PRODUCT AND P.CATEGORY = ? "
                + "ORDER BY O.CATORDER, P.NAME ", SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead()).list(category);
    }

    /**
     *
     * @param category
     * @return
     * @throws BasicException
     */
    public List<ProductInfoExt> getAllProductCatalogByCatOrder() throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.CODETYPE, "
                + "P.NAME, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.TAXCAT, "
                + "P.CATEGORY, "
                + "P.ATTRIBUTESET_ID, "
                + "P.IMAGE, "
                + "P.ATTRIBUTES, "
                + "P.ISKITCHEN, "
                + "P.ISSERVICE, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.ALIAS, "
                + "P.ALWAYSAVAILABLE, "
                + "P.DISCOUNTED, "
                + "P.CANDISCOUNT, "
                + "P.ISPACK, P.PACKQUANTITY, P.PACKPRODUCT "
                + "FROM PRODUCTS P, PRODUCTS_CAT O "
                + "WHERE P.ID = O.PRODUCT "
                + "ORDER BY O.CATORDER, P.NAME ", null, ProductInfoExt.getSerializerRead()).list();
    }

    public List<ProductInfoExt> getAllNonProductCatalog() throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.NAME, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.TAXCAT, "
                + "P.CATEGORY, "
                + "P.ATTRIBUTESET_ID, "
                + "P.IMAGE, "
                + "P.ATTRIBUTES, "
                + "P.ISKITCHEN, "
                + "P.ISSERVICE, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.ALIAS, "
                + "P.ALWAYSAVAILABLE, "
                + "P.DISCOUNTED, "
                + "P.CANDISCOUNT, "
                + "P.ISPACK, P.PACKQUANTITY, P.PACKPRODUCT "
                + "FROM PRODUCTS P "
                + "WHERE NOT EXISTS (SELECT O.PRODUCT FROM PRODUCTS_CAT O WHERE P.ID = O.PRODUCT) "
                + "ORDER BY P.CATEGORY, P.NAME ", null, ProductInfoExt.getSerializerRead()).list();
    }

    /**
     *
     * @param category
     * @return
     * @throws BasicException
     */
    public List<ProductInfoExt> getAllProductCatalog() throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.CODETYPE, "
                + "P.NAME, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.TAXCAT, "
                + "P.CATEGORY, "
                + "P.ATTRIBUTESET_ID, "
                + "P.IMAGE, "
                + "P.ATTRIBUTES, "
                + "P.ISKITCHEN, "
                + "P.ISSERVICE, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.ALIAS, "
                + "P.ALWAYSAVAILABLE, "
                + "P.DISCOUNTED, "
                + "P.CANDISCOUNT, "
                + "P.ISPACK, P.PACKQUANTITY, P.PACKPRODUCT "
                + "FROM PRODUCTS P, PRODUCTS_CAT O "
                + "WHERE P.ID = O.PRODUCT "
                + "ORDER BY P.CATEGORY, P.NAME ", null, ProductInfoExt.getSerializerRead()).list();
    }

    /**
     *
     * @param category
     * @return
     * @throws BasicException
     */
    public List<ProductInfoExt> getProductCatalogAlways() throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "PRODUCTS.ID, "
                + "PRODUCTS.REFERENCE, "
                + "PRODUCTS.CODE, "
                + "PRODUCTS.CODETYPE, "
                + "PRODUCTS.NAME, "
                + "PRODUCTS.ISCOM, "
                + "PRODUCTS.ISSCALE, "
                + "PRODUCTS.PRICEBUY, "
                + "PRODUCTS.PRICESELL, "
                + "PRODUCTS.TAXCAT, "
                + "PRODUCTS.CATEGORY, "
                + "PRODUCTS.ATTRIBUTESET_ID, "
                + "PRODUCTS.IMAGE, "
                + "PRODUCTS.ATTRIBUTES, "
                + "PRODUCTS.ISKITCHEN, "
                + "PRODUCTS.ISSERVICE, "
                + "PRODUCTS.DISPLAY, "
                + "PRODUCTS.ISVPRICE, "
                + "PRODUCTS.ISVERPATRIB, "
                + "PRODUCTS.TEXTTIP, "
                + "PRODUCTS.WARRANTY, "
                + "PRODUCTS.STOCKUNITS, "
                + "PRODUCTS.ALIAS, "
                + "PRODUCTS.ALWAYSAVAILABLE, "
                + "PRODUCTS.DISCOUNTED, "
                + "PRODUCTS.CANDISCOUNT, "
                + "PRODUCTS.ISPACK, "
                + "PRODUCTS.PACKQUANTITY, "
                + "PRODUCTS.PACKPRODUCT "
                + "FROM CATEGORIES INNER JOIN PRODUCTS ON (PRODUCTS.CATEGORY = CATEGORIES.ID) "
                + "WHERE PRODUCTS.ALWAYSAVAILABLE = " + s.DB.TRUE() + " "
                + "ORDER BY  CATEGORIES.NAME, PRODUCTS.NAME",
                null,
                ProductInfoExt.getSerializerRead()).list();

    }

    public List<ProductInfoExt> getProductNonCatalog(String category) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.NAME, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.TAXCAT, "
                + "P.CATEGORY, "
                + "P.ATTRIBUTESET_ID, "
                + "P.IMAGE, "
                + "P.ATTRIBUTES, "
                + "P.ISKITCHEN, "
                + "P.ISSERVICE, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.ALIAS, "
                + "P.ALWAYSAVAILABLE, "
                + "P.DISCOUNTED, "
                + "P.CANDISCOUNT, "
                + "P.ISPACK, P.PACKQUANTITY, P.PACKPRODUCT "
                + "FROM PRODUCTS P "
                + "WHERE NOT EXISTS (SELECT O.PRODUCT FROM PRODUCTS_CAT O WHERE P.ID = O.PRODUCT) "
                + "AND P.CATEGORY = ? "
                + "ORDER BY P.NAME ", SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead()).list(category);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public List<ProductInfoExt> getProductComments(String id) throws BasicException {
        return new PreparedSentence(s, "SELECT "
                + "P.ID, "
                + "P.REFERENCE, "
                + "P.CODE, "
                + "P.CODETYPE, "
                + "P.NAME, "
                + "P.ISCOM, "
                + "P.ISSCALE, "
                + "P.PRICEBUY, "
                + "P.PRICESELL, "
                + "P.TAXCAT, "
                + "P.CATEGORY, "
                + "P.ATTRIBUTESET_ID, "
                + "P.IMAGE, "
                + "P.ATTRIBUTES, "
                + "P.ISKITCHEN, "
                + "P.ISSERVICE, "
                + "P.DISPLAY, "
                + "P.ISVPRICE, "
                + "P.ISVERPATRIB, "
                + "P.TEXTTIP, "
                + "P.WARRANTY, "
                + "P.STOCKUNITS, "
                + "P.ALIAS, "
                + "P.ALWAYSAVAILABLE, "
                + "P.DISCOUNTED, "
                + "P.CANDISCOUNT, "
                + "P.ISPACK, "
                + "P.PACKQUANTITY, "
                + "P.PACKPRODUCT "
                + "FROM PRODUCTS P, "
                + "PRODUCTS_CAT O, PRODUCTS_COM M "
                + "WHERE P.ID = O.PRODUCT AND P.ID = M.PRODUCT2 AND M.PRODUCT = ? "
                + "AND P.ISCOM = " + s.DB.TRUE() + " "
                + "ORDER BY O.CATORDER, P.NAME", SerializerWriteString.INSTANCE, ProductInfoExt.getSerializerRead()).list(id);
    }

    /**
     *
     * @return @throws BasicException
     */
    public List<PromoInfo> getCurrentPromos() throws BasicException {

        return new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "STARTHOUR, "
                + "ENDHOUR, "
                + "ARTICLE, "
                + "ARTICLECATEGORY, "
                + "TYPE, "
                + "AMOUNT, "
                + "QTYMIN, "
                + "QTYMAX, "
                + "QTYSTEP, "
                + "AMOUNTSTEP, "
                + "BONUSARTICLE, "
                + "BONUSARTICLEDESC "
                + "FROM PROMO_HEADER "
                + "WHERE DATE(concat(substring(startdate, 1,4), "
                + "'-',substring(startdate, 5,2), '-',substring(startdate, 7,2))) <= current_date "
                + "AND DATE(concat(substring(enddate, 1,4),'-',substring(enddate, 5,2),'-',substring(enddate, 7,2))) >= current_date "
                + "AND time(concat(starthour,':00:00')) <= current_time AND time(concat(endhour,':00:00')) >= current_time "
                + "ORDER BY TYPE DESC", null, PromoInfo.getSerializerRead()).list();
    }

    /**
     *
     * @return @throws BasicException
     */
    public PromoInfo[] getPromos() throws BasicException {

        List<PromoInfo> _promos = getCurrentPromos();
        PromoInfo[] _tabpromo = new PromoInfo[_promos.size()];
        return _promos.toArray(_tabpromo);

    }

    /**
     *
     * @return
     */
    public final SentenceList getPromoTypeList() {
        return new StaticSentence(s, "SELECT ID, "
                + "DESCRIPTION "
                + "FROM PROMO_TYPE "
                + "ORDER BY ID", null, new SerializerReadClass(PromoTypeInfo.class));
    }

    /**
     *
     * @param id
     * @return
     */
    public final SentenceList getCatName(String id) {
        return new StaticSentence(s, "SELECT "
                + "ID "
                + "FROM CATEGORIES WHERE ID = ?", null, new SerializerReadClass(PromoTypeInfo.class));
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final CategoryInfo getCategoryInfo(String id) throws BasicException {
        return (CategoryInfo) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "COLOUR, "
                + "CATORDER "
                + "FROM CATEGORIES "
                + "WHERE ID = ? "
                + "ORDER BY NAME", SerializerWriteString.INSTANCE, CategoryInfo.getSerializerRead()).find(id);
    }

    public final SentenceList getProductList() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, " //1,2,3,4
                + "ISCOM, ISSCALE, " //5,6
                + "PRICEBUY, PRICESELL, " //7,8
                + "TAXCAT, CATEGORY, " //9,10
                + "ATTRIBUTESET_ID, " //11
                + "IMAGE, ATTRIBUTES, " //12,13
                + "ISKITCHEN, ISSERVICE, " //14,15
                + "DISPLAY, " //16
                + "ISVPRICE, ISVERPATRIB, " //17,18
                + "TEXTTIP, WARRANTY, " //19,20
                + "STOCKCURRENT.UNITS, " //21
                + "ALIAS, " //22
                + "ALWAYSAVAILABLE, " //23 
                + "DISCOUNTED, CANDISCOUNT, "
                + "ISPACK, PACKQUANTITY, PACKPRODUCT "
                + "FROM STOCKCURRENT RIGHT OUTER JOIN PRODUCTS ON (STOCKCURRENT.PRODUCT = PRODUCTS.ID) "
                + "WHERE ?(QBF_FILTER) "
                + "ORDER BY REFERENCE, NAME",
                new String[]{"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE", "UNITS"}), new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,}), ProductInfoExt.getSerializerRead());
    }

    public SentenceList getProductListNormal() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, " //1,2,3,4
                + "ISCOM, ISSCALE, " //5,6
                + "PRICEBUY, PRICESELL, " //7,8
                + "TAXCAT, CATEGORY, " //9,10
                + "ATTRIBUTESET_ID, " //11
                + "IMAGE, ATTRIBUTES, " //12,13
                + "ISKITCHEN, ISSERVICE, " //14,15
                + "DISPLAY, " //16
                + "ISVPRICE, ISVERPATRIB, " //17,18
                + "TEXTTIP, WARRANTY, " //19,20
                + "STOCKCURRENT.UNITS, " //21
                + "ALIAS, " //22
                + "ALWAYSAVAILABLE, " //23 
                + "DISCOUNTED, CANDISCOUNT, "
                + "ISPACK, PACKQUANTITY, PACKPRODUCT "
                + "FROM STOCKCURRENT RIGHT OUTER JOIN PRODUCTS ON (STOCKCURRENT.PRODUCT = PRODUCTS.ID) "
                + "WHERE ISCOM = " + s.DB.FALSE() + " AND ?(QBF_FILTER) "
                + "ORDER BY REFERENCE, NAME",
                new String[]{"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE", "UNITS"}), new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE
        }), ProductInfoExt.getSerializerRead());
    }

    public SentenceList getProductListAuxiliar() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, " //1,2,3,4
                + "ISCOM, ISSCALE, " //5,6
                + "PRICEBUY, PRICESELL, " //7,8
                + "TAXCAT, CATEGORY, " //9,10
                + "ATTRIBUTESET_ID, " //11
                + "IMAGE, ATTRIBUTES, " //12,13
                + "ISKITCHEN, ISSERVICE, " //14,15
                + "DISPLAY, " //16
                + "ISVPRICE, ISVERPATRIB, " //17,18
                + "TEXTTIP, WARRANTY, " //19,20
                + "STOCKCURRENT.UNITS, " //21
                + "ALIAS, " //22
                + "ALWAYSAVAILABLE, " //23  
                + "DISCOUNTED, CANDISCOUNT, "
                + "ISPACK, PACKQUANTITY, PACKPRODUCT "
                + "FROM STOCKCURRENT RIGHT OUTER JOIN PRODUCTS ON (STOCKCURRENT.PRODUCT = PRODUCTS.ID) "
                + "WHERE ISCOM = " + s.DB.TRUE() + " AND ?(QBF_FILTER) "
                + "ORDER BY REFERENCE", new String[]{"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"}), new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}), ProductInfoExt.getSerializerRead());
    }

    //Tickets and Receipt list
    /**
     *
     * @return
     */
    public SentenceList getTicketsList() {
        return new StaticSentence(s, new QBFBuilder(
                "SELECT "
                + "T.TICKETID, "
                + "T.TICKETTYPE, "
                + "R.DATENEW, "
                + "P.NAME, "
                + "C.NAME, "
                + "SUM(PM.TOTAL) "
                + "FROM RECEIPTS "
                + "R JOIN TICKETS T ON R.ID = T.ID LEFT OUTER JOIN PAYMENTS PM "
                + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN CUSTOMERS C "
                + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                + "WHERE ?(QBF_FILTER) "
                + "GROUP BY "
                + "T.ID, "
                + "T.TICKETID, "
                + "T.TICKETTYPE, "
                + "R.DATENEW, "
                + "P.NAME, "
                + "C.NAME "
                + "ORDER BY R.DATENEW DESC, T.TICKETID",
                new String[]{"T.TICKETID", "T.TICKETTYPE", "PM.TOTAL", "R.DATENEW", "R.DATENEW", "P.NAME", "C.NAME"}), new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.INT,
            Datas.OBJECT, Datas.INT,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.TIMESTAMP,
            Datas.OBJECT, Datas.TIMESTAMP,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}), new SerializerReadClass(FindTicketsInfo.class));
    }

    //User list
    /**
     *
     * @return
     */
    public final SentenceList getUserList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM PEOPLE "
                + "ORDER BY NAME", null, new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                return new TaxCategoryInfo(
                        dr.getString(1),
                        dr.getString(2));
            }
        });
    }

    // Listados para combo
    /**
     *
     * @return
     */
    public final SentenceList getTaxList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "CATEGORY, "
                + "CUSTCATEGORY, "
                + "PARENTID, "
                + "RATE, "
                + "RATECASCADE, "
                + "RATEORDER "
                + "FROM TAXES "
                + "ORDER BY NAME", null, new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                return new TaxInfo(
                        dr.getString(1),
                        dr.getString(2),
                        dr.getString(3),
                        dr.getString(4),
                        dr.getString(5),
                        dr.getDouble(6),
                        dr.getBoolean(7),
                        dr.getInt(8));
            }
        });
    }

    /**
     *
     * @return
     */
    public final SentenceList getCategoriesList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "COLOUR, "
                + "CATORDER "
                + "FROM CATEGORIES "
                + "ORDER BY NAME", null, CategoryInfo.getSerializerRead());
    }

      
    /**
     *
     * @return
     */
    public final SentenceList getTaxCustCategoriesList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM TAXCUSTCATEGORIES "
                + "ORDER BY NAME", null, new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                return new TaxCustCategoryInfo(dr.getString(1), dr.getString(2));
            }
        });
    }

    /**
     *
     * @return @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public final List<CustomerTransaction> getCustomersTransactionList() throws BasicException {
        return new PreparedSentence(s,
                "SELECT TICKETS.TICKETID, PRODUCTS.NAME AS PNAME, "
                + "SUM(TICKETLINES.UNITS) AS UNITS, "
                + "SUM(TICKETLINES.UNITS * TICKETLINES.PRICE) AS AMOUNT, "
                + "SUM(TICKETLINES.UNITS * TICKETLINES.PRICE * (1.0 + TAXES.RATE)) AS TOTAL, "
                + "RECEIPTS.DATENEW, CUSTOMERS.NAME AS CNAME "
                + "FROM RECEIPTS, CUSTOMERS, TICKETS, TICKETLINES "
                + "LEFT OUTER JOIN PRODUCTS ON TICKETLINES.PRODUCT = PRODUCTS.ID "
                + "LEFT OUTER JOIN TAXES ON TICKETLINES.TAXID = TAXES.ID  "
                + "WHERE CUSTOMERS.ID = TICKETS.CUSTOMER AND TICKETLINES.PRODUCT = PRODUCTS.ID AND RECEIPTS.ID = TICKETS.ID AND TICKETS.ID = TICKETLINES.TICKET "
                + "GROUP BY CUSTOMERS.NAME, RECEIPTS.DATENEW, TICKETS.TICKETID, PRODUCTS.NAME, TICKETS.TICKETTYPE "
                + "ORDER BY RECEIPTS.DATENEW DESC, PRODUCTS.NAME",
                null,
                CustomerTransaction.getSerializerRead()).list();
    }

    @SuppressWarnings("unchecked")
    public final List<CustomerTransaction> getCustomersTransactionList(String name) throws BasicException {
        return new PreparedSentence(s,
                "SELECT TICKETS.TICKETID, PRODUCTS.NAME AS PNAME, "
                + "SUM(TICKETLINES.UNITS) AS UNITS, "
                + "SUM(TICKETLINES.UNITS * TICKETLINES.PRICE) AS AMOUNT, "
                + "SUM(TICKETLINES.UNITS * TICKETLINES.PRICE * (1.0 + TAXES.RATE)) AS TOTAL, "
                + "RECEIPTS.DATENEW, CUSTOMERS.NAME AS CNAME "
                + "FROM RECEIPTS, CUSTOMERS, TICKETS, TICKETLINES "
                + "LEFT OUTER JOIN PRODUCTS ON TICKETLINES.PRODUCT = PRODUCTS.ID "
                + "LEFT OUTER JOIN TAXES ON TICKETLINES.TAXID = TAXES.ID  "
                + "WHERE CUSTOMERS.ID = TICKETS.CUSTOMER AND TICKETLINES.PRODUCT = PRODUCTS.ID AND RECEIPTS.ID = TICKETS.ID AND TICKETS.ID = TICKETLINES.TICKET "
                + "AND CUSTOMERS.NAME = \"" + name + "\" "
                + "GROUP BY CUSTOMERS.NAME, RECEIPTS.DATENEW, TICKETS.TICKETID, PRODUCTS.NAME, TICKETS.TICKETTYPE "
                + "ORDER BY RECEIPTS.DATENEW DESC, PRODUCTS.NAME",
                null,
                CustomerTransaction.getSerializerRead()).list();
    }

    /**
     *
     * @return
     */
    public final SentenceList getTaxCategoriesList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM TAXCATEGORIES "
                + "ORDER BY NAME", null, new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                return new TaxCategoryInfo(dr.getString(1), dr.getString(2));
            }
        });
    }

    /**
     *
     * @return
     */
    public final SentenceList getAttributeSetList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM ATTRIBUTESET "
                + "ORDER BY NAME", null, new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                return new AttributeSetInfo(dr.getString(1), dr.getString(2));
            }
        });
    }

    /**
     *
     * @return
     */
    public final SentenceList getLocationsList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME, "
                + "ADDRESS FROM LOCATIONS "
                + "ORDER BY NAME", null, new SerializerReadClass(LocationInfo.class));
    }

    /**
     *
     * @return
     */
    public final SentenceList getFloorsList() {
        return new StaticSentence(s, "SELECT ID, NAME FROM FLOORS ORDER BY NAME", null, new SerializerReadClass(FloorsInfo.class));
    }

    /**
     *
     * @param card
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt findCustomerExt(String card) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "TAXID, "
                + "SEARCHKEY, "
                + "NAME, "
                + "CARD, "
                + "TAXCATEGORY, "
                + "NOTES, "
                + "MAXDEBT, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "IMAGE "
                + "FROM CUSTOMERS "
                + "WHERE CARD = ? AND VISIBLE = " + s.DB.TRUE() + " "
                + "ORDER BY NAME", SerializerWriteString.INSTANCE, new CustomerExtRead()).find(card);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public CustomerInfoExt loadCustomerExt(String id) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s, "SELECT "
                + "ID, "
                + "TAXID, "
                + "SEARCHKEY, "
                + "NAME, "
                + "CARD, "
                + "TAXCATEGORY, "
                + "NOTES, "
                + "MAXDEBT, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "IMAGE "
                + "FROM CUSTOMERS WHERE ID = ?", SerializerWriteString.INSTANCE, new CustomerExtRead()).find(id);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final boolean isCashActive(String id) throws BasicException {

        return new PreparedSentence(s,
                "SELECT MONEY FROM CLOSEDCASH WHERE DATEEND IS NULL AND MONEY = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE).find(id)
                != null;
    }

    /**
     *
     * @param tickettype
     * @param ticketid
     * @return
     * @throws BasicException
     */
    public final TicketInfo loadTicket(final int tickettype, final int ticketid) throws BasicException {
        TicketInfo ticket = (TicketInfo) new PreparedSentence(s, "SELECT "
                + "T.ID, "
                + "T.TICKETTYPE, "
                + "T.TICKETID, "
                + "R.DATENEW, "
                + "R.MONEY, "
                + "R.ATTRIBUTES, "
                + "P.ID, "
                + "P.NAME, "
                + "T.CUSTOMER "
                + "FROM RECEIPTS R "
                + "JOIN TICKETS T ON R.ID = T.ID "
                + "LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                + "WHERE T.TICKETTYPE = ? AND T.TICKETID = ? "
                + "ORDER BY R.DATENEW DESC", SerializerWriteParams.INSTANCE, new SerializerReadClass(TicketInfo.class))
                .find(new DataParams() {
                    @Override
                    public void writeValues() throws BasicException {
                        setInt(1, tickettype);
                        setInt(2, ticketid);
                    }
                });
        if (ticket != null) {

            String customerid = ticket.getCustomerId();
            ticket.setCustomer(customerid == null
                    ? null
                    : loadCustomerExt(customerid));

            ticket.setLines(new PreparedSentence(s, "SELECT L.TICKET, L.LINE, L.PRODUCT, L.ATTRIBUTESETINSTANCE_ID, L.UNITS, L.PRICE, T.ID, T.NAME, T.CATEGORY, T.CUSTCATEGORY, T.PARENTID, T.RATE, T.RATECASCADE, T.RATEORDER, L.ATTRIBUTES, L.REFUNDQTY  "
                    + "FROM TICKETLINES L, TAXES T WHERE L.TAXID = T.ID AND L.TICKET = ? ORDER BY L.LINE", SerializerWriteString.INSTANCE, new SerializerReadClass(TicketLineInfo.class)).list(ticket.getId()));
            ticket.setPayments(new PreparedSentence(s //                    , "SELECT PAYMENT, TOTAL, TRANSID TENDERED FROM PAYMENTS WHERE RECEIPT = ?" 
                    , "SELECT PAYMENT, TOTAL, TRANSID, TENDERED, CARDNAME FROM PAYMENTS WHERE RECEIPT = ?", SerializerWriteString.INSTANCE, new SerializerReadClass(PaymentInfoTicket.class)).list(ticket.getId()));
        }
        return ticket;
    }

    /**
     *
     * @param ticket
     * @param location
     * @throws BasicException
     */
    public final void saveTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t;
        t = new Transaction(s) {
            @Override
            public Object transact() throws BasicException {

                // Set Receipt Id
                switch (ticket.getTicketType()) {
                    case NORMAL:
                        ticket.setTicketId(getNextTicketIndex());
                        break;
                    case REFUND:
                        ticket.setTicketId(getNextTicketRefundIndex());
                        break;
                    case PAYMENT:
                        ticket.setTicketId(getNextTicketPaymentIndex());
                        break;
                    case NOSALE:
                        ticket.setTicketId(getNextTicketPaymentIndex());
                        break;
                    case INVOICE:
                        ticket.setTicketId(getNextTicketInvoiceIndex());
                        break;

                    default:
                        throw new BasicException();
                }

                new PreparedSentence(s, "INSERT INTO RECEIPTS (ID, MONEY, DATENEW, ATTRIBUTES, PERSON) VALUES (?, ?, ?, ?, ?)", SerializerWriteParams.INSTANCE
                ).exec(new DataParams() {
                    @Override
                    public void writeValues() throws BasicException {
                        setString(1, ticket.getId());
                        setString(2, ticket.getActiveCash());
                        setTimestamp(3, ticket.getDate());
                        try {
                            ByteArrayOutputStream o = new ByteArrayOutputStream();
                            ticket.getProperties().storeToXML(o, AppLocal.APP_NAME, "UTF-8");
                            setBytes(4, o.toByteArray());
                        } catch (IOException e) {
                            setBytes(4, null);
                        }
                        setString(5, ticket.getProperty("person"));
                    }
                }
                );

                // new ticket
                new PreparedSentence(s, "INSERT INTO TICKETS (ID, TICKETTYPE, TICKETID, PERSON, CUSTOMER) VALUES (?, ?, ?, ?, ?)", SerializerWriteParams.INSTANCE
                ).exec(new DataParams() {
                    @Override
                    public void writeValues() throws BasicException {
                        setString(1, ticket.getId());
                        setInt(2, ticket.getTicketType().getId());
                        setInt(3, ticket.getTicketId());
                        setString(4, ticket.getUser().getId());
                        setString(5, ticket.getCustomerId());
                    }
                }
                );

                SentenceExec ticketlineinsert = new PreparedSentence(s, "INSERT INTO TICKETLINES (TICKET, LINE, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, TAXID, ATTRIBUTES, REFUNDQTY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", SerializerWriteBuilder.INSTANCE);

                for (TicketLineInfo l : ticket.getLines()) {

                    ticketlineinsert.exec(l);

                    if (l.getProductID() != null && l.isProductService() != true) {
                        // update the stock
                        getStockDiaryInsert().exec(new Object[]{
                            UUID.randomUUID().toString(),
                            ticket.getDate(),
                            l.getMultiply() < 0.0
                            ? MovementReason.IN_REFUND.getKey()
                            : MovementReason.OUT_SALE.getKey(),
                            location,
                            l.getProductID(),
                            l.getProductAttSetInstId(), -l.getMultiply(), l.getPrice(),
                            ticket.getUser().getName()
                        });
                    }

                }
                final Payments payments = new Payments();
                SentenceExec paymentinsert = new PreparedSentence(s //            , "INSERT INTO PAYMENTS (ID, RECEIPT, PAYMENT, TOTAL, TRANSID, RETURNMSG, TENDERED) VALUES (?, ?, ?, ?, ?, ?, ?)"
                        , "INSERT INTO PAYMENTS (ID, RECEIPT, PAYMENT, TOTAL, TRANSID, RETURNMSG, TENDERED, CARDNAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", SerializerWriteParams.INSTANCE);

                for (final PaymentInfo p : ticket.getPayments()) {
                    payments.addPayment(p.getName(), p.getTotal(), p.getPaid(), ticket.getReturnMessage());
                }

                //for (final PaymentInfo p : ticket.getPayments()) {
                while (payments.getSize() >= 1) {
                    paymentinsert.exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            pName = payments.getFirstElement();
                            getTotal = payments.getPaidAmount(pName);
                            getTendered = payments.getTendered(pName);
                            getRetMsg = payments.getRtnMessage(pName);
                            payments.removeFirst(pName);

                            setString(1, UUID.randomUUID().toString());
                            setString(2, ticket.getId());
                            setString(3, pName);
                            setDouble(4, getTotal);
                            setString(5, ticket.getTransactionID());
                            setBytes(6, (byte[]) Formats.BYTEA.parseValue(getRetMsg));
                            setDouble(7, getTendered);
                            setString(8, getCardName);
                            payments.removeFirst(pName);
                        }
                    });

                    if ("debt".equals(pName) || "debtpaid".equals(pName)) {
                        // udate customer fields...
                        ticket.getCustomer().updateCurDebt(getTotal, ticket.getDate());
                        // save customer fields...
                        getDebtUpdate().exec(new DataParams() {
                            @Override
                            public void writeValues() throws BasicException {
                                setDouble(1, ticket.getCustomer().getCurdebt());
                                setTimestamp(2, ticket.getCustomer().getCurdate());
                                setString(3, ticket.getCustomer().getId());
                            }
                        });
                    }
                }

                SentenceExec taxlinesinsert = new PreparedSentence(s, "INSERT INTO TAXLINES (ID, RECEIPT, TAXID, BASE, AMOUNT)  VALUES (?, ?, ?, ?, ?)", SerializerWriteParams.INSTANCE);
                if (ticket.getTaxes() != null) {
                    for (final TicketTaxInfo tickettax : ticket.getTaxes()) {
                        taxlinesinsert.exec(new DataParams() {
                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, UUID.randomUUID().toString());
                                setString(2, ticket.getId());
                                setString(3, tickettax.getTaxInfo().getId());
                                setDouble(4, tickettax.getSubTotal());
                                setDouble(5, tickettax.getTax());
                            }
                        });
                    }
                }

                return null;
            }
        };
        t.execute();
    }

    /**
     *
     * @param ticket
     * @param location
     * @throws BasicException
     */
    public final void deleteTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t = new Transaction(s) {
            @Override
            public Object transact() throws BasicException {

                // update the inventory
                Date d = new Date();
                for (int i = 0; i < ticket.getLinesCount(); i++) {
                    if (ticket.getLine(i).getProductID() != null) {
                        // Hay que actualizar el stock si el hay producto
                        getStockDiaryInsert().exec(new Object[]{
                            UUID.randomUUID().toString(),
                            d,
                            ticket.getLine(i).getMultiply() >= 0.0
                            ? MovementReason.IN_REFUND.getKey()
                            : MovementReason.OUT_SALE.getKey(),
                            location,
                            ticket.getLine(i).getProductID(),
                            ticket.getLine(i).getProductAttSetInstId(), ticket.getLine(i).getMultiply(), ticket.getLine(i).getPrice(),
                            ticket.getUser().getName()
                        });
                    }
                }

                // update customer debts
                for (PaymentInfo p : ticket.getPayments()) {
                    if ("debt".equals(p.getName()) || "debtpaid".equals(p.getName())) {

                        // udate customer fields...
                        ticket.getCustomer().updateCurDebt(-p.getTotal(), ticket.getDate());

                        // save customer fields...
                        getDebtUpdate().exec(new DataParams() {
                            @Override
                            public void writeValues() throws BasicException {
                                setDouble(1, ticket.getCustomer().getCurdebt());
                                setTimestamp(2, ticket.getCustomer().getCurdate());
                                setString(3, ticket.getCustomer().getId());
                            }
                        });
                    }
                }

                // and delete the receipt
                new StaticSentence(s, "DELETE FROM TAXLINES WHERE RECEIPT = ?", SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s, "DELETE FROM PAYMENTS WHERE RECEIPT = ?", SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s, "DELETE FROM TICKETLINES WHERE TICKET = ?", SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s, "DELETE FROM TICKETS WHERE ID = ?", SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s, "DELETE FROM RECEIPTS WHERE ID = ?", SerializerWriteString.INSTANCE).exec(ticket.getId());
                return null;
            }
        };
        t.execute();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextPickupIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "PICKUP_NUMBER").find();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextTicketIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "TICKETSNUM").find();
    }

    public final Integer getNextTicketInvoiceIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "TICKETSNUM_INVOICE").find();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextTicketRefundIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "TICKETSNUM_REFUND").find();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextTicketPaymentIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "TICKETSNUM_PAYMENT").find();
    }

    /**
     *
     * @return
     */
    public final SentenceList getProductCatQBF() {
        return new StaticSentence(s, new QBFBuilder(
                //			"SELECT P.ID, P.REFERENCE, P.CODE, P.NAME, P.ISCOM, P.ISSCALE, P.PRICEBUY, P.PRICESELL, P.CATEGORY, P.TAXCAT, P.ATTRIBUTESET_ID, P.IMAGE, P.STOCKCOST, P.STOCKVOLUME, CASE WHEN C.PRODUCT IS NULL THEN " + s.DB.FALSE() + " ELSE " + s.DB.TRUE() + " END, C.CATORDER, P.ATTRIBUTES, P.ISKITCHEN, P.ISSERVICE, P.DISPLAY, P.ISVPRICE, P.ISVERPATRIB, P.TEXTTIP, P.WARRANTY, P.PRINTKB, P.SENDSTATUS " +
                "SELECT P.ID, "
                + "P.REFERENCE, P.CODE, P.CODETYPE, P.NAME, P.ISCOM, "
                + "P.ISSCALE, P.PRICEBUY, P.PRICESELL, "
                + "P.CATEGORY, P.TAXCAT, P.ATTRIBUTESET_ID, "
                + "P.IMAGE, P.STOCKCOST, P.STOCKVOLUME, "
                + "CASE WHEN C.PRODUCT IS NULL "
                + "THEN " + s.DB.FALSE() + " ELSE " + s.DB.TRUE() + " END, "
                + "C.CATORDER, P.ATTRIBUTES, P.ISKITCHEN, "
                + "P.ISSERVICE, P.DISPLAY, P.ISVPRICE, "
                + "P.ISVERPATRIB, P.TEXTTIP, P.WARRANTY, P.STOCKUNITS, "
                + "P.ALIAS, P.ALWAYSAVAILABLE, "
                + "P.DISCOUNTED, P.CANDISCOUNT, "
                + "P.ISPACK, P.PACKQUANTITY, P.PACKPRODUCT "
                + "FROM PRODUCTS P LEFT OUTER JOIN PRODUCTS_CAT C "
                + "ON P.ID = C.PRODUCT "
                + "WHERE ?(QBF_FILTER) "
                + "ORDER BY P.REFERENCE",
                new String[]{
                    "P.NAME", "P.PRICEBUY", "P.PRICESELL", "P.CATEGORY", "P.CODE"}), new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}), productsRow.getSerializerRead());
    }

    /**
     *
     * @return
     */
    public final SentenceExec getProductCatInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s, "INSERT INTO PRODUCTS (ID, "
                        + "REFERENCE, CODE, CODETYPE, NAME, ISCOM, "
                        + "ISSCALE, PRICEBUY, PRICESELL, CATEGORY, TAXCAT, "
                        + "ATTRIBUTESET_ID, IMAGE, STOCKCOST, STOCKVOLUME, " //14
                        + "ATTRIBUTES, ISKITCHEN, ISSERVICE, DISPLAY, ISVPRICE, "
                        + "ISVERPATRIB, TEXTTIP, WARRANTY, STOCKUNITS, ALIAS, ALWAYSAVAILABLE, DISCOUNTED, CANDISCOUNT, " //25
                        + "ISPACK, PACKQUANTITY, PACKPRODUCT  ) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new SerializerWriteBasicExt(productsRow.getDatas(),
                                new int[]{0,
                                    1, 2, 3, 4,
                                    5, 6, 7, 8, 9,
                                    10, 11, 12, 13, 14,
                                    17, 18, 19, 20,
                                    21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32})).exec(params);

                new PreparedSentence(s, "INSERT INTO STOCKCURRENT (LOCATION, PRODUCT, UNITS) VALUES ('0', ?, 0.0)", new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0})).exec(params);

                if (i > 0 && ((Boolean) values[15])) {
                    return new PreparedSentence(s, "INSERT INTO PRODUCTS_CAT (PRODUCT, CATORDER) VALUES (?, ?)", new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0, 16})).exec(params);
                } else {
                    return i;
                }
            }
        };
    }

    public final SentenceList getPackProductList() {
        return new StaticSentence(s, "SELECT "
                + "ID, "
                + "NAME "
                + "FROM PRODUCTS "
                + "ORDER BY NAME", null, new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                return new PackProductInfo(dr.getString(1), dr.getString(2));
            }
        });
    }

    /**
     *
     * @return
     */
    public final SentenceExec getProductCatUpdate() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                Object[] values = (Object[]) params;
                int i = new PreparedSentence(s, "UPDATE PRODUCTS SET ID = ?, REFERENCE = ?, "
                        + "CODE = ?, CODETYPE = ?, NAME = ?, ISCOM = ?, "
                        + "ISSCALE = ?, PRICEBUY = ?, "
                        + "PRICESELL = ?, CATEGORY = ?, "
                        + "TAXCAT = ?, ATTRIBUTESET_ID = ?, "
                        + "IMAGE = ?, STOCKCOST = ?, "
                        + "STOCKVOLUME = ?, ATTRIBUTES = ?, "
                        + "ISKITCHEN = ?, ISSERVICE = ?, "
                        + "DISPLAY = ?, ISVPRICE = ?, "
                        + "ISVERPATRIB = ?, TEXTTIP = ?, "
                        + "WARRANTY = ?, ALIAS = ?, ALWAYSAVAILABLE = ?, "
                        + "DISCOUNTED = ?, CANDISCOUNT = ?, "
                        + "ISPACK = ?, PACKQUANTITY = ?, PACKPRODUCT = ? "
                        + "WHERE ID = ?", new SerializerWriteBasicExt(productsRow.getDatas(),
                                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                    10, 11, 12, 13, 14, 17, 18, 19, 20,
                                    21, 22, 23, 24, 26, 27, 28, 29, 30, 31, 32, 0})).exec(params);
                if (i > 0) {
                    if (((Boolean) values[15])) {
                        if (new PreparedSentence(s, "UPDATE PRODUCTS_CAT SET CATORDER = ? WHERE PRODUCT = ?", new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{16, 0})).exec(params) == 0) {
                            new PreparedSentence(s, "INSERT INTO PRODUCTS_CAT (PRODUCT, CATORDER) VALUES (?, ?)", new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0, 16})).exec(params);
                        }
                    } else {
                        new PreparedSentence(s, "DELETE FROM PRODUCTS_CAT WHERE PRODUCT = ?", new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0})).exec(params);
                    }
                }
                return i;
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getProductCatDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s, "DELETE FROM STOCKCURRENT WHERE PRODUCT = ?", new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0})).exec(params);
                new PreparedSentence(s, "DELETE FROM PRODUCTS_CAT WHERE PRODUCT = ?", new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0})).exec(params);
                return new PreparedSentence(s, "DELETE FROM PRODUCTS WHERE ID = ?", new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0})).exec(params);

            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getDebtUpdate() {

        return new PreparedSentence(s, "UPDATE CUSTOMERS SET CURDEBT = ?, CURDATE = ? WHERE ID = ?", SerializerWriteParams.INSTANCE);
    }

    /**
     *
     * @return
     */
    public final SentenceExec getStockDiaryInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                int updateresult = ((Object[]) params)[5] == null // si ATTRIBUTESETINSTANCE_ID is null
                        ? new PreparedSentence(s, "UPDATE STOCKCURRENT SET UNITS = (UNITS + ?) WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID IS NULL", new SerializerWriteBasicExt(stockdiaryDatas, new int[]{6, 3, 4})).exec(params)
                        : new PreparedSentence(s, "UPDATE STOCKCURRENT SET UNITS = (UNITS + ?) WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID = ?", new SerializerWriteBasicExt(stockdiaryDatas, new int[]{6, 3, 4, 5})).exec(params);

                if (updateresult == 0) {
                    new PreparedSentence(s, "INSERT INTO STOCKCURRENT (LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS) VALUES (?, ?, ?, ?)", new SerializerWriteBasicExt(stockdiaryDatas, new int[]{3, 4, 5, 6})).exec(params);
                }
                return new PreparedSentence(s, "INSERT INTO STOCKDIARY (ID, DATENEW, REASON, LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, AppUser) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", new SerializerWriteBasicExt(stockdiaryDatas, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8})).exec(params);
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getStockDiaryDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                int updateresult = ((Object[]) params)[5] == null // if ATTRIBUTESETINSTANCE_ID is null
                        ? new PreparedSentence(s, "UPDATE STOCKCURRENT SET UNITS = (UNITS - ?) WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID IS NULL", new SerializerWriteBasicExt(stockdiaryDatas, new int[]{6, 3, 4})).exec(params)
                        : new PreparedSentence(s, "UPDATE STOCKCURRENT SET UNITS = (UNITS - ?) WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID = ?", new SerializerWriteBasicExt(stockdiaryDatas, new int[]{6, 3, 4, 5})).exec(params);

                if (updateresult == 0) {
                    new PreparedSentence(s, "INSERT INTO STOCKCURRENT (LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS) VALUES (?, ?, ?, -(?))", new SerializerWriteBasicExt(stockdiaryDatas, new int[]{3, 4, 5, 6})).exec(params);
                }
                return new PreparedSentence(s, "DELETE FROM STOCKDIARY WHERE ID = ?", new SerializerWriteBasicExt(stockdiaryDatas, new int[]{0})).exec(params);
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getPaymentMovementInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s, "INSERT INTO RECEIPTS (ID, MONEY, DATENEW) VALUES (?, ?, ?)", new SerializerWriteBasicExt(paymenttabledatas, new int[]{0, 1, 2})).exec(params);
                return new PreparedSentence(s, "INSERT INTO PAYMENTS (ID, RECEIPT, PAYMENT, TOTAL, NOTES) VALUES (?, ?, ?, ?, ?)", new SerializerWriteBasicExt(paymenttabledatas, new int[]{3, 0, 4, 5, 6})).exec(params);
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getPaymentMovementDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s, "DELETE FROM PAYMENTS WHERE ID = ?", new SerializerWriteBasicExt(paymenttabledatas, new int[]{3})).exec(params);
                return new PreparedSentence(s, "DELETE FROM RECEIPTS WHERE ID = ?", new SerializerWriteBasicExt(paymenttabledatas, new int[]{0})).exec(params);
            }
        };
    }

    /**
     *
     * @param warehouse
     * @param id
     * @param attsetinstid
     * @return
     * @throws BasicException
     */
    public final double findProductStock(String warehouse, String id, String attsetinstid) throws BasicException {

        PreparedSentence p = attsetinstid == null
                ? new PreparedSentence(s, "SELECT UNITS FROM STOCKCURRENT WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID IS NULL", new SerializerWriteBasic(Datas.STRING, Datas.STRING), SerializerReadDouble.INSTANCE)
                : new PreparedSentence(s, "SELECT UNITS FROM STOCKCURRENT WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID = ?", new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING), SerializerReadDouble.INSTANCE);

        Double d = (Double) p.find(warehouse, id, attsetinstid);
        return d == null ? 0.0 : d;
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCatalogCategoryAdd() {
        return new StaticSentence(s, "INSERT INTO PRODUCTS_CAT(PRODUCT, CATORDER) SELECT ID, " + s.DB.INTEGER_NULL() + " FROM PRODUCTS WHERE CATEGORY = ?", SerializerWriteString.INSTANCE);
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCatalogCategoryDel() {
        return new StaticSentence(s, "DELETE FROM PRODUCTS_CAT WHERE PRODUCT = ANY (SELECT ID FROM PRODUCTS WHERE CATEGORY = ?)", SerializerWriteString.INSTANCE);
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableCategories() {
        return new TableDefinition(s,
                "CATEGORIES", new String[]{"ID", "NAME", "PARENTID", "IMAGE", 
                    "TEXTTIP", "CATSHOWNAME", "COLOUR", "CATORDER"}, 
                new String[]{"ID", AppLocal.getIntString("Label.Name"), "", 
                    AppLocal.getIntString("label.image")}, new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.IMAGE, Datas.STRING, Datas.BOOLEAN, Datas.STRING, Datas.INT}, new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.NULL, Formats.STRING, Formats.BOOLEAN, Formats.STRING, Formats.INT}, new int[]{0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxes() {
        return new TableDefinition(s,
                "TAXES", new String[]{"ID", "NAME", "CATEGORY", "CUSTCATEGORY", "PARENTID", "RATE", "RATECASCADE", "RATEORDER"}, new String[]{"ID", AppLocal.getIntString("Label.Name"), AppLocal.getIntString("label.taxcategory"), AppLocal.getIntString("label.custtaxcategory"), AppLocal.getIntString("label.taxparent"), AppLocal.getIntString("label.dutyrate"), AppLocal.getIntString("label.cascade"), AppLocal.getIntString("label.order")}, new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.BOOLEAN, Datas.INT}, new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.PERCENT, Formats.BOOLEAN, Formats.INT}, new int[]{0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxCustCategories() {
        return new TableDefinition(s,
                "TAXCUSTCATEGORIES", new String[]{"ID", "NAME"}, new String[]{"ID", AppLocal.getIntString("Label.Name")}, new Datas[]{Datas.STRING, Datas.STRING}, new Formats[]{Formats.STRING, Formats.STRING}, new int[]{0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxCategories() {
        return new TableDefinition(s,
                "TAXCATEGORIES", new String[]{"ID", "NAME"}, new String[]{"ID", AppLocal.getIntString("Label.Name")}, new Datas[]{Datas.STRING, Datas.STRING}, new Formats[]{Formats.STRING, Formats.STRING}, new int[]{0}
        );
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableLocations() {
        return new TableDefinition(s,
                "LOCATIONS", new String[]{"ID", "NAME", "ADDRESS"}, new String[]{"ID", AppLocal.getIntString("label.locationname"), AppLocal.getIntString("label.locationaddress")}, new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING}, new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING}, new int[]{0}
        );
    }

    public final void updateRefundQty(Double qty, String ticket, Integer line) throws BasicException {
        m_updateRefund.exec(qty, ticket, line);
    }

    public final boolean getVoucher(String id) throws BasicException {
        return new PreparedSentence(s,
                "SELECT SOLDTICKETID FROM VOUCHERS WHERE VOUCHER = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE).find(id)
                != null;
    }

    public final void sellVoucher(Object[] voucher) throws BasicException {
        m_sellvoucher.exec(voucher);
    }

    /**
     *
     */
    protected static class CustomerExtRead implements SerializerRead {

        /**
         *
         * @param dr
         * @return
         * @throws BasicException
         */
        @Override
        public Object readValues(DataRead dr) throws BasicException {
            CustomerInfoExt c = new CustomerInfoExt(dr.getString(1));
            c.setTaxid(dr.getString(2));
            c.setSearchkey(dr.getString(3));
            c.setName(dr.getString(4));
            c.setCard(dr.getString(5));
            c.setTaxCustomerID(dr.getString(6));
            c.setNotes(dr.getString(7));
            c.setMaxdebt(dr.getDouble(8));
            c.setVisible(dr.getBoolean(9));
            c.setCurdate(dr.getTimestamp(10));
            c.setCurdebt(dr.getDouble(11));
            c.setFirstname(dr.getString(12));
            c.setLastname(dr.getString(13));
            c.setEmail(dr.getString(14));
            c.setPhone(dr.getString(15));
            c.setPhone2(dr.getString(16));
            c.setFax(dr.getString(17));
            c.setAddress(dr.getString(18));
            c.setAddress2(dr.getString(19));
            c.setPostal(dr.getString(20));
            c.setCity(dr.getString(21));
            c.setRegion(dr.getString(22));
            c.setCountry(dr.getString(23));
            c.setImage(dr.getString(24));

            return c;
        }
    }
}
