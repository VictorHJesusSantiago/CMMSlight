import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h2>CMMSlight</h2>
        <nav>
          <NavLink to="/" end>Dashboard</NavLink>
          <NavLink to="/assets">Ativos</NavLink>
          <NavLink to="/work-orders">Ordens de Servico</NavLink>
          <NavLink to="/maintenance-plans">Manutencao Preventiva</NavLink>
          <NavLink to="/parts">Estoque</NavLink>
          <NavLink to="/failures">Falhas / MTBF-MTTR</NavLink>
        </nav>
        <div className="sidebar-footer">
          {user && (
            <>
              <div className="user-info">
                <strong>{user.name}</strong>
                <span>{user.role}</span>
              </div>
              <button onClick={handleLogout}>Sair</button>
            </>
          )}
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
