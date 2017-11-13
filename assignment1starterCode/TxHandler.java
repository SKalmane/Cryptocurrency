import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class TxHandler {

    private UTXOPool utxoPool;
    
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
	this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
	ArrayList<Transaction.Input> inputs = tx.getInputs();
	ArrayList<UTXO> usedUTXOs;
	double totalOutputValue = 0;
	double totalInputValue = 0;
	
	/* (1) all outputs claimed by {@code tx} are in the current UTXO pool
	 * (2) the signatures on each input of {@code tx} are valid, *
	 * (3) no UTXO is claimed multiple times by {@code tx}, */
	for(Transaction.Input in : inputs) {
	    UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
	    if(!utxoPool.contains(utxo)) return false;
	    
	    byte[] rawData = tx.getRawDataToSign(in.outputIndex);
	    Transaction.Output spentOutput = utxoPool.getTxOutput(utxo);
	    if(!Crypto.verifySignature(spentOutput.address, rawData, in.signature)) return false;

	    if(usedUTXOs.contains(utxo)) {
		return false;
	    } else {
		usedUTXOs.add(utxo);
	    }
	    totalInputValue += spentOutput.value;
	}

	ArrayList<Transaction.Output> outputs = tx.getOutputs();
	for(Transaction.Output out : outputs) {
	    if(out.value < 0) return false;
	    totalOutputValue += out.value;
	}

	if(totalInputValue < totalOutputValue) return false;
	
	return true;

    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
	Transaction[] validTxs;
	for(int i = 0; i < possibleTxs.length; i++) {
	    if(!isValidTx(possibleTxs[i])) continue;
	    validTxs.add(possibleTxs[i]);
	    ArrayList<Transaction.Input> inputs = tx.getInputs();
	    for(Transaction.Input in : inputs) {
		UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
		this.utxoPool.removeUTXO(utxo);
	    }
	}
	return validTxs;
    }
    
}
