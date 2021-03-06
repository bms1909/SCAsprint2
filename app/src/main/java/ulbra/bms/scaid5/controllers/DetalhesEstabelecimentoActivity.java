package ulbra.bms.scaid5.controllers;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsCategorias;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsEstabelecimentos;


public class DetalhesEstabelecimentoActivity extends ActionBarActivity {

    private clsEstabelecimentos estabCarregado;
    private RatingBar rb;
    private boolean jaAvaliado;

    //carrega todas as informações do estabelecimento a partir do objeto global
    private void atualizaTela() {
        ActionBar ab = getSupportActionBar();
        ab.setTitle(clsCategorias.getNomeCategoria(estabCarregado.idCategoria));

        TextView tvTitulo = (TextView) findViewById(R.id.tv_estabelecimento_nome);
        tvTitulo.setText(estabCarregado.nomeEstabelecimento);
        TextView tvEndereco = (TextView) findViewById(R.id.tv_estabelecimento_endereco);
        tvEndereco.setText(estabCarregado.enderecoEstabelecimento);
        TextView tvCidade = (TextView) findViewById(R.id.tv_estabelecimento_cidade);
        tvCidade.setText(estabCarregado.cidadeEstabelecimento);
        TextView tvFone = (TextView) findViewById(R.id.tv_estabelecimento_telefone);
        tvFone.setText(estabCarregado.telefoneEstabelecimento);

        rb = (RatingBar) findViewById(R.id.rb_estabelecimento_classificacao);
        rb.setMax(5);
        rb.setRating(estabCarregado.mediaEstrelasAtendimento);


        CheckBox cbxBanheiro = (CheckBox) findViewById(R.id.cbx_estabelecimento_possui_banheiro);

        cbxBanheiro.setChecked(estabCarregado.possuiBanheiro);
        CheckBox cbxEstacionamento = (CheckBox) findViewById(R.id.cbx_estabelecimento_possui_estacionamento);
        cbxEstacionamento.setChecked(estabCarregado.possuiEstacionamento);
        CheckBox cbxAltura = (CheckBox) findViewById(R.id.cbx_estabelecimento_altura_certa);
        cbxAltura.setChecked(estabCarregado.alturaCerta);
        CheckBox cbxRampa = (CheckBox) findViewById(R.id.cbx_estabelecimento_possui_rampa);
        cbxRampa.setChecked(estabCarregado.possuiRampa);
        CheckBox cbxLargura = (CheckBox) findViewById(R.id.cbx_estabelecimento_largura_suficiente);
        cbxLargura.setChecked(estabCarregado.larguraSuficiente);
    }

    public void salvaAvaliacao_Click(View a) {
        // envia ao WS a avaliação do estabelecimento
        rb = (RatingBar) findViewById(R.id.rb_estabelecimento_classificacao);
        //TODO,obter id do usuário
        estabCarregado.avaliaEstabelecimento(rb.getProgress(), 1, this);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_estabelecimento);

        rb = (RatingBar) findViewById(R.id.rb_estabelecimento_classificacao);

        final AlertDialog.Builder dlgAlert = new AlertDialog.Builder(DetalhesEstabelecimentoActivity.this);
        dlgAlert.setTitle("Confirmação");
        //TODO ver se dá pra recuperar nota
        dlgAlert.setMessage("Estabelecimento já avaliado com a nota x, deseja alterar?");


        //listener de pressionamento das estrelas de avaliação, quando acionado, altera altura da buttonBar e exibe botão "avaliar"
        rb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final LinearLayout btnBar = (LinearLayout) findViewById(R.id.btnbar_estabelecimento);
                    final ViewGroup.LayoutParams a = btnBar.getLayoutParams();
                    a.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                    if ((jaAvaliado) && (btnBar.getHeight() == 0)) {
                        dlgAlert.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //inicia tela de detalhes do estabelecimento, enviado o ID do mesmo via putExtra
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    btnBar.setLayoutParams(a);
                                }
                            }
                        });
                        dlgAlert.setNegativeButton("Não", null);
                        dlgAlert.create().show();
                    } else {
                        btnBar.setLayoutParams(a);
                    }
                }
                return false;
            }
        });

        //recebe os dados da outra página e carrega o objeto local
        Intent recebido = getIntent();
        estabCarregado = new clsEstabelecimentos(recebido.getIntExtra("ID_ESTABELECIMENTO", 0));
        estabCarregado = estabCarregado.carregaDetalhesEstabelecimento();
        atualizaTela();
        //TODO colocar id do usuário
        jaAvaliado = clsEstabelecimentos.estabelecimentoFoiAvaliado(1, estabCarregado.idEstabelecimento);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // executado ao criar o menu, o código abaixo confere se há algum app que responda a intent, se não houver,
        // remove o botão correspondente a ação
        getMenuInflater().inflate(R.menu.menu_detalhes_estabelecimento, menu);

        final PackageManager mgr = this.getPackageManager();
        List<ResolveInfo> list =
                mgr.queryIntentActivities(new Intent(Intent.ACTION_VIEW, Uri.parse("geo: -29.331124, -49.751402")),
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0)
            menu.removeItem(R.id.btnEstabelecimentoAbrirMaps);

        list = mgr.queryIntentActivities(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:9999")),
                PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0)
            menu.removeItem(R.id.btnEstabelecimentoTelefone);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //tratamento de botões na activityBar

        switch (item.getItemId()) {
            case R.id.btnEstabelecimentoAbrirMaps:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + estabCarregado.latlonEstabelecimento.latitude + "," + estabCarregado.latlonEstabelecimento.longitude + "(" + Uri.encode(estabCarregado.nomeEstabelecimento) + ")")));
                break;
            case R.id.btnEstabelecimentoTelefone:
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + estabCarregado.telefoneEstabelecimento)));
                break;
            case R.id.btnEstabelecimentoEditar:
                Toast.makeText(this, "em breve", Toast.LENGTH_SHORT).show();
                break;
           /*TODO código para envio de dados a página de cadastro previamente preenchida

                rb.setIsIndicator(false);
                */
        }

        return super.onOptionsItemSelected(item);
    }
}
