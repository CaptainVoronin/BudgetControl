package org.max.budgetcontrol;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import org.max.budgetcontrol.datasource.AZenClientResponseHandler;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

class DlgCheckConnection extends Dialog
{
   AZenClientResponseHandler requestHandler;


   public DlgCheckConnection(Context context, AZenClientResponseHandler requestHandler )
   {
      super(context);
      this.requestHandler = requestHandler;
   }

   public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the Builder class for convenient dialog construction.
      AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
      LayoutInflater inflater = getLayoutInflater();
      builder.setView(inflater.inflate(R.layout.dlg_check_connection, null));
      builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int id) {
            requestHandler.canceRequest();
            cancel();
         }
      });
      return builder.create();
   }
}
