package ttt.scan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ttt.scan.widgets.ScanShapeView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, ScanActivity.class);
        switch (v.getId()){
            case R.id.vid_am_square_btn: // 正方形
                intent.putExtra("type", ScanShapeView.Shape.Square.name());
                break;
            case R.id.vid_am_hexagon_btn: // 六边形
                intent.putExtra("type", ScanShapeView.Shape.Hexagon.name());
                break;
            case R.id.vid_am_annulus_btn: // 环形
                intent.putExtra("type", ScanShapeView.Shape.Annulus.name());
                break;
        }
        // 跳转页面
        startActivity(intent);
    }
}
