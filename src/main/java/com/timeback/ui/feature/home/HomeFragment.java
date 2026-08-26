package com.timeback.ui.feature.home;

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
 * UI-02 홈 대시보드 화면.
 *
 * 처리하는 ScreenState:
 * - Initial: 초기 상태
 * - Loading: 데이터 로딩 중
 * - Content(HomeViewData): 오늘의 낭비 시간, 기준선, 목표 진행률 표시
 * - RetryableError(OFFLINE): 오프라인 → 캐시 데이터 + 재시도 버튼
 * - Empty: 데이터 없음 (첫 사용)
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
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
                // TODO: 로딩 스피너 표시
            } else if (state instanceof ScreenState.Content) {
                // TODO: HomeViewData로 대시보드 렌더링
                //   - 오늘 낭비 시간 카드
                //   - 기준선 상태 (관찰 중 / 확정)
                //   - 목표별 진행률 리스트
                //   - 절약/되찾기 수치
            } else if (state instanceof ScreenState.RetryableError) {
                // TODO: 에러 메시지 + 재시도 버튼
            } else if (state instanceof ScreenState.Empty) {
                // TODO: 온보딩 안내 표시
            }
        });
    }
}
