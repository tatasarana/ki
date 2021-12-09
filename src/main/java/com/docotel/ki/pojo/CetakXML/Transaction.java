package com.docotel.ki.pojo.CetakXML;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name="Transaction")
@XmlAccessorType(XmlAccessType.NONE)
public class Transaction {
    private String xmlns;
    private String xmlnswo;
    private TradeMarkTransactionBody tradeMarkTransactionBody;

    public TradeMarkTransactionBody getTradeMarkTransactionBody() {
        return tradeMarkTransactionBody;
    }

    @XmlElement(name = "TradeMarkTransactionBody")
    public void setTradeMarkTransactionBody(TradeMarkTransactionBody tradeMarkTransactionBody) {
        this.tradeMarkTransactionBody = tradeMarkTransactionBody;
    }

    public String getXmlns() {
        return xmlns;
    }

    @XmlAttribute
    public void setXmlns(String xmlns) {
        this.xmlns = "http://www.wipo.int/standards/XMLSchema/trademarks";
    }

    public String getXmlnswo() {
        return xmlnswo;
    }

    @XmlAttribute(name = "xmlns:wo")
    public void setXmlnswo(String xmlnswo) {
        this.xmlnswo = "http://www.wipo.int/standards/XMLSchema/wo-trademarks";
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class TradeMarkTransactionBody{
        public TransactionContentDetails TransactionContentDetails;

        public TransactionContentDetails getTransactionContentDetails() {
            return TransactionContentDetails;
        }

        @XmlElement(name="TransactionContentDetails")
        public void setTransactionContentDetails(TransactionContentDetails transactionContentDetails) {
            TransactionContentDetails = transactionContentDetails;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class TransactionContentDetails{
        public String TransactionIdentifier;
        public String TransactionCode;
        public String TransactionSubCode;
        public TransactionData TransactionData;

        public String getTransactionIdentifier() {
            return TransactionIdentifier;
        }

        @XmlElement(name="TransactionIdentifier")
        public void setTransactionIdentifier(String transactionIdentifier) {
            TransactionIdentifier = transactionIdentifier;
        }

        public String getTransactionCode() {
            return TransactionCode;
        }

        @XmlElement(name="TransactionCode")
        public void setTransactionCode(String transactionCode) {
            TransactionCode = transactionCode;
        }

        public String getTransactionSubCode() {
            return TransactionSubCode;
        }

        @XmlElement(name="TransactionSubCode")
        public void setTransactionSubCode(String transactionSubCode) {
            TransactionSubCode = transactionSubCode;
        }

        public Transaction.TransactionData getTransactionData() {
            return TransactionData;
        }

        @XmlElement(name="TransactionData")
        public void setTransactionData(Transaction.TransactionData transactionData) {
            TransactionData = transactionData;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class TransactionData{
        public TradeMarkApplication TradeMarkApplication;

        public Transaction.TradeMarkApplication getTradeMarkApplication() {
            return TradeMarkApplication;
        }

        @XmlElement(name="TradeMarkApplication")
        public void setTradeMarkApplication(Transaction.TradeMarkApplication tradeMarkApplication) {
            TradeMarkApplication = tradeMarkApplication;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class TradeMarkApplication{
        public TradeMarkDetails TradeMarkDetails;

        public Transaction.TradeMarkDetails getTradeMarkDetails() {
            return TradeMarkDetails;
        }

        @XmlElement(name="TradeMarkDetails")
        public void setTradeMarkDetails(Transaction.TradeMarkDetails tradeMarkDetails) {
            TradeMarkDetails = tradeMarkDetails;
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class TradeMarkDetails{
        public List<TradeMark> TradeMark;

        public List<TradeMark> getTradeMark() {
            return TradeMark;
        }

        @XmlElement(name="TradeMark")
        public void setTradeMark(List<TradeMark> tradeMark) {
            TradeMark = tradeMark;
        }
    }
}
