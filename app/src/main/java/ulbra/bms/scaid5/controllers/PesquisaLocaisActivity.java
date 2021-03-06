package ulbra.bms.scaid5.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsApiClientSingleton;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsCategorias;
import ulbra.bms.scaid5.ulbra.bms.scaid5.models.clsEstabelecimentos;


public class PesquisaLocaisActivity extends ActionBarActivity {

    //busca local de emissão do alerta
    private final LatLng localAtual = clsApiClientSingleton.ultimoLocal(this);
    public ListView lista;
    private ArrayList<clsEstabelecimentos> estabelecimentosCarregados;
    private ArrayList<clsCategorias> categoriasCarregadas = clsCategorias.carregaCategorias();
    private List<Map<String, String>> elementosLista = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesquisa_locais);
        final EditText busca = (EditText) findViewById(R.id.txt_busca);
        Spinner sp = (Spinner) findViewById(R.id.sp_busca_raio);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float raio = 1;
                switch (position) {
                    case 0:
                        raio = 0.5f;
                        break;
                    case 1:
                        raio = 2;
                        break;
                    case 2:
                        raio = 5;
                        break;
                    case 3:
                        raio = 10;
                        break;
                    case 4:
                        raio = 25;
                        break;
                }
                if (localAtual == null) {
                    gpsDesligado();
                } else {
                    estabelecimentosCarregados = clsEstabelecimentos.estabelecimentosPorRaio(raio, localAtual);
                }
                EditText pesquisa = (EditText) findViewById(R.id.txt_busca);
                if (pesquisa.getText().length() > 0) {
                    buscaTexto(pesquisa.getText());
                }
                populaLista();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        busca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscaTexto(s);
                populaLista();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        lista = (ListView) findViewById(R.id.list_busca);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map selecionado = elementosLista.get(position);
                if (selecionado.get("linha0").equals("e")) {
                    startActivity(new Intent(PesquisaLocaisActivity.this, DetalhesEstabelecimentoActivity.class).putExtra("ID_ESTABELECIMENTO", Integer.parseInt(selecionado.get("linha1").toString())));
                } else if (selecionado.get("linha0").equals("c")) {
                    startActivity(new Intent(PesquisaLocaisActivity.this, PesquisaCategoriaActivity.class).putExtra("ID_CATEGORIA", Integer.parseInt(selecionado.get("linha1").toString())));
                }
            }
        });
        populaLista();
    }

    private void gpsDesligado() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //cria uma caixa de diálogo caso o GPS esteja desligado
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    } else {
                        finish();
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Localização Desativada");
            builder.setMessage("Este aplicativo utiliza sua localização com Alta Precisão (GPS), deseja habilitar agora?");
            //se o malandro pressionar fora do AlertDialog, fecha o aplicativo
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });

            builder.setPositiveButton("Sim", dialogClickListener).setNegativeButton("Não", dialogClickListener);
            builder.create().show();
        }
    }

    private void buscaTexto(CharSequence parametro) {
        elementosLista.clear();
        for (clsCategorias busca : categoriasCarregadas) {
            if (busca.getNomeCategoria().contains(parametro)) {
                Map<String, String> m = new HashMap<>();
                m.put("linha0", "c");
                m.put("linha1", "" + busca.getIdCategoria());
                m.put("linha2", busca.getNomeCategoria());
                m.put("linha3", "Categoria");
                elementosLista.add(m);
            }
        }
        for (clsEstabelecimentos busca : estabelecimentosCarregados) {
            if (busca.nomeEstabelecimento.contains(parametro)) {
                Map<String, String> m = new HashMap<>();
                m.put("linha0", "e");
                m.put("linha1", "" + busca.idEstabelecimento);
                m.put("linha2", busca.nomeEstabelecimento);

                for (clsCategorias nome : categoriasCarregadas) {
                    if (nome.getIdCategoria() == busca.idCategoria) {
                        m.put("linha3", nome.getNomeCategoria());
                        break;
                    }
                }
                elementosLista.add(m);
            }
        }
        //TODO adicionar lógica para busca de endereço aqui
    }

    private void populaLista() {
        String[] from = {"linha2", "linha3"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        lista.setAdapter(new SimpleAdapter(this, elementosLista, android.R.layout.simple_list_item_2, from, to));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pesquisa_locais, menu);
        //código de exemplo do google para criação de activity pesquisável
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
