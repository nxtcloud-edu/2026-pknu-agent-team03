package com.timeback.ui.feature.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.timeback.R;

import com.timeback.ui.domain.model.ScreenState;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * UI-07 리포트 화면.
 *
 * 처리하는 ScreenState:
 * - Loading: 리포트 데이터 로딩 중
 * - Content(ReportData): 기간별 통계 표시
 * - Empty: 기준선 미확정 → 데이터 부족 안내
 * - Error: 로딩 실패
 *
 * 사용자 액션:
 * - 기간 변경 (DAILY / WEEKLY / MONTHLY)
 */
@AndroidEntryPoint
public class ReportFragment extends Fragment {

    private ReportViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeState();
    }

    private void observeState() {
        viewModel.state.observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ScreenState.Loading) {
                // TODO: 로딩 표시
            } else if (state instanceof ScreenState.Content) {
                // TODO: ReportData로 리포트 렌더링
                //   - 낭비/절약/되찾기 시간 카드
                //   - 기준선 대비 변화량
                //   - 목표별 달성률 리스트
                //   - 기간 선택 탭 (Daily/Weekly/Monthly)
            } else if (state instanceof ScreenState.Empty) {
                // TODO: "기준선이 확정되면 리포트를 볼 수 있어요" 안내
            } else if (state instanceof ScreenState.Error) {
                // TODO: 에러 + 재시도
            }
        });
    }
}
